package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.domain.entity.DisAbroadDistribution
import cz.machovec.lekovyportal.domain.entity.DisAbroadPurchaserType
import cz.machovec.lekovyportal.domain.entity.MovementType
import cz.machovec.lekovyportal.domain.entity.ProcessedDataset
import cz.machovec.lekovyportal.domain.repository.DisAbroadDistributionRepository
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.NewFileMessage
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URL

@Service
class DisAbroadFileProcessor(
    private val disAbroadDistributionRepository: DisAbroadDistributionRepository,
    private val processedDatasetRepository: ProcessedDatasetRepository,
) : DatasetFileProcessor {

    private val logger = KotlinLogging.logger {}

    @Transactional
    override fun processFile(msg: NewFileMessage) {
        val isProcessed = processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(
            msg.datasetType, msg.year, msg.month ?: 0
        )

        if (isProcessed) {
            logger.info { "Dataset ${msg.datasetType} for ${msg.year}-${msg.month} already processed. Skipping." }
            return
        }

        val fileBytes = URL(msg.fileUrl).readBytes()
        val records = parseCsv(fileBytes)

        logger.info("Processing file: ${msg.fileUrl}, records count: ${records.size}")

        disAbroadDistributionRepository.saveAll(records)

        val processedDataset = ProcessedDataset(
            datasetType = msg.datasetType,
            year = msg.year,
            month = msg.month ?: 0
        )
        processedDatasetRepository.save(processedDataset)

        logger.info { "Dataset ${msg.datasetType} for ${msg.year}-${msg.month} marked as processed." }
    }

    private fun parseCsv(csvBytes: ByteArray): List<DisAbroadDistribution> {
        val text = csvBytes.decodeToString()
        val lines = text.split("\r\n", "\n").filter { it.isNotBlank() }

        val records = mutableListOf<DisAbroadDistribution>()

        lines.drop(1).forEachIndexed { index, line ->
            val cols = line.split(";").map { it.trim('"') }

            try {
                val periodParts = cols[0].split(".")
                val year = periodParts[0].toIntOrNull() ?: throw IllegalArgumentException("Invalid year in period")
                val month = periodParts[1].toIntOrNull() ?: throw IllegalArgumentException("Invalid month in period")

                val purchaserType = DisAbroadPurchaserType.fromString(cols[1])
                    ?: throw IllegalArgumentException("Invalid purchaser type: ${cols[1]}")

                val suklCode = cols[3]

                val movementType = MovementType.fromString(cols[8])
                    ?: throw IllegalArgumentException("Invalid movement type: ${cols[8]}")

                val packageCount = cols[9].toIntOrNull() ?: throw IllegalArgumentException("Invalid package count")

                val subject = cols[10]

                val record = DisAbroadDistribution(
                    year = year,
                    month = month,
                    purchaserType = purchaserType,
                    suklCode = suklCode,
                    movementType = movementType,
                    packageCount = packageCount,
                    subject = subject,
                )

                records.add(record)
            } catch (e: Exception) {
                logger.error { "Error parsing line ${index + 2}: ${e.message} | Raw data: $line" }
            }
        }
        return records
    }
}
