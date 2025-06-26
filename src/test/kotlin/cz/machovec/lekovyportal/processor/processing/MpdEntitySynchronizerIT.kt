package cz.machovec.lekovyportal.processor.processing

import cz.machovec.lekovyportal.core.domain.mpd.MpdCountry
import cz.machovec.lekovyportal.core.domain.mpd.MpdDatasetType
import cz.machovec.lekovyportal.core.repository.mpd.MpdCountryRepository
import cz.machovec.lekovyportal.processor.processing.mpd.MpdEntitySynchronizer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate

@SpringBootTest
@Testcontainers
class MpdEntitySynchronizerIT @Autowired constructor(
    val repo: MpdCountryRepository,
    val synchronizer: MpdEntitySynchronizer
) {

    companion object {
        @Container
        private val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("lp_test")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun datasourceConfig(reg: DynamicPropertyRegistry) {
            reg.add("spring.datasource.url", postgres::getJdbcUrl)
            reg.add("spring.datasource.username", postgres::getUsername)
            reg.add("spring.datasource.password", postgres::getPassword)
        }

        private val T0 = LocalDate.of(2024, 12, 1)   // previous dataset date
        private val T1 = LocalDate.of(2025,  1, 1)   // current snapshot date
    }

    @Test
    fun `insert update and missing are handled correctly`() {
        /* ---------- initialize DB to represent T0 snapshot ---------- */
        val czOld = repo.save(
            MpdCountry(
                code = "CZ",
                name = "Czechia",
                nameEn = "Czech Republic",
                edqmCode = null,
                firstSeen = T0,
                missingSince = null
            )
        )

        val deOld = repo.save(
            MpdCountry(
                code = "DE",
                name = "Německo",
                nameEn = "Germany",
                edqmCode = null,
                firstSeen = T0,
                missingSince = null
            )
        )

        /* ---------- new records from T1 snapshot ---------- */
        val czNew = czOld.copy(                             // nameEn changed → update
            nameEn = "Czechia",
            firstSeen = T1                                  // synchronizer will retain original firstSeen
        )

        val skNew = MpdCountry(                             // new record → insert
            code = "SK",
            name = "Slovensko",
            nameEn = "Slovakia",
            edqmCode = null,
            firstSeen = T1,
            missingSince = null
        )

        /* ---------- run synchronization ---------- */
        synchronizer.sync(
            validFrom = T1,
            dataset = MpdDatasetType.MPD_COUNTRY,
            records = listOf(czNew, skNew),
            repo = repo
        )

        /* ---------- verify results ---------- */
        val all = repo.findAll().associateBy { it.code }

        // INSERT – SK must exist and have firstSeen = T1
        val sk = all["SK"]!!
        assertThat(sk.firstSeen).isEqualTo(T1)
        assertThat(sk.missingSince).isNull()

        // UPDATE – CZ should exist, retain same ID and original firstSeen, nameEn updated
        val cz = all["CZ"]!!
        assertThat(cz.id).isEqualTo(czOld.id)
        assertThat(cz.firstSeen).isEqualTo(T0)
        assertThat(cz.nameEn).isEqualTo("Czechia")

        // MISSING – DE was not in input, should have missingSince = T1
        val de = all["DE"]!!
        assertThat(de.missingSince).isEqualTo(T1)
    }
}

