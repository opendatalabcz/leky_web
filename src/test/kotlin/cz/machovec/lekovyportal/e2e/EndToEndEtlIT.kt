package cz.machovec.lekovyportal.e2e

import com.fasterxml.jackson.databind.ObjectMapper
import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.core.domain.dataset.FileType
import cz.machovec.lekovyportal.core.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.dto.DatasetToProcessMessage
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("End-to-end ETL smoke-test (Scraper → RabbitMQ → Processor → DB)")
class EndToEndEtlIT @Autowired constructor(
    private val rabbit: RabbitTemplate,
    private val processedRepo: ProcessedDatasetRepository,
    private val jdbc: JdbcTemplate
) {

    companion object {
        @Container
        private val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine")
            .apply { withDatabaseName("lp_test"); withUsername("test"); withPassword("test") }

        @Container
        private val rabbitMq = RabbitMQContainer(DockerImageName.parse("rabbitmq:3.12-management"))

        init {
            postgres.start()
            rabbitMq.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun configure(reg: DynamicPropertyRegistry) {
            reg.add("spring.datasource.url")      { postgres.jdbcUrl }
            reg.add("spring.datasource.username") { postgres.username }
            reg.add("spring.datasource.password") { postgres.password }

            reg.add("spring.rabbitmq.host")     { rabbitMq.host }
            reg.add("spring.rabbitmq.port")     { rabbitMq.amqpPort }
            reg.add("spring.rabbitmq.username") { rabbitMq.adminUsername }
            reg.add("spring.rabbitmq.password") { rabbitMq.adminPassword }
        }
    }

    @Test
    fun `dataset is processed once and import is idempotent`() {
        // Step 1: Prepare and send MPD dataset for 2021-01
        val mpdZipBytes = ClassPathResource("testdata/dlp202101.zip")
            .inputStream.readAllBytes()
        val mpdTempZip: Path = Files.createTempFile("dlp_202101", ".zip")
        Files.write(mpdTempZip, mpdZipBytes)

        val mpdMsg = DatasetToProcessMessage(
            datasetType = DatasetType.MEDICINAL_PRODUCT_DATABASE,
            year = 2021,
            month = 1,
            fileUrl = "file:$mpdTempZip",
            fileType = FileType.ZIP
        )

        rabbit.convertAndSend("datasetExchange", "dataset.new", mpdMsg)

        await()
            .atMost(Duration.ofSeconds(60))
            .until {
                processedRepo.existsByDatasetTypeAndYearAndMonth(
                    DatasetType.MEDICINAL_PRODUCT_DATABASE, 2021, 1
                )
            }

        // Verify MPD records were imported
        val mpdCodes = jdbc.queryForList(
            "SELECT sukl_code FROM mpd_medicinal_product WHERE sukl_code IN ('A12345', 'B67890')",
            String::class.java
        )
        assertThat(mpdCodes).containsExactlyInAnyOrder("A12345", "B67890")

        // Step 2: Prepare and send eRecept dispense dataset for 2021-01
        val zipBytes = ClassPathResource("testdata/erecept_vydej_202101.zip")
            .inputStream.readAllBytes()
        val tempZip: Path = Files.createTempFile("dispenses_202101", ".zip")
        Files.write(tempZip, zipBytes)

        val msg = DatasetToProcessMessage(
            datasetType = DatasetType.ERECEPT_DISPENSES,
            year = 2021,
            month = 1,
            fileUrl = "file:$tempZip",
            fileType = FileType.ZIP
        )

        rabbit.convertAndSend("datasetExchange", "dataset.new", msg)

        await()
            .atMost(Duration.ofSeconds(60))
            .until {
                processedRepo.existsByDatasetTypeAndYearAndMonth(
                    DatasetType.ERECEPT_DISPENSES, 2021, 1
                )
            }

        val all = jdbc.queryForList("SELECT * FROM erecept_dispense")
        println("ERECEPT_DISPENSE records: ${all.size}")

        val countSql = """
        SELECT COUNT(*) 
          FROM erecept_dispense 
         WHERE year = 2021 
           AND month = 1
    """.trimIndent()

        val rowsBefore = jdbc.queryForObject(countSql, Int::class.java)!!
        assertThat(rowsBefore).isGreaterThan(0)

        // Step 3: Re-send the same dispense message to test idempotence
        rabbit.convertAndSend("datasetExchange", "dataset.new", msg)
        await().pollDelay(Duration.ofSeconds(5)).until { true }

        val rowsAfter = jdbc.queryForObject(countSql, Int::class.java)!!
        assertThat(rowsAfter).isEqualTo(rowsBefore)
    }
}
