package cz.machovec.lekovyportal.processor.dist

import cz.machovec.lekovyportal.domain.entity.distribution.MovementType
import cz.machovec.lekovyportal.domain.entity.ProcessedDataset
import cz.machovec.lekovyportal.domain.entity.distribution.DistFromMahs
import cz.machovec.lekovyportal.domain.entity.distribution.MahPurchaserType
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.domain.repository.dist.DistFromMahsRepository
import cz.machovec.lekovyportal.messaging.DatasetToProcessMessage
import cz.machovec.lekovyportal.processor.DatasetProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset

@Service
class DistFromMahsProcessor(
    private val distFromMahsRepository: DistFromMahsRepository,
    private val processedDatasetRepository: ProcessedDatasetRepository,
    private val referenceDataProvider: MpdReferenceDataProvider
) : DatasetProcessor {

    private val logger = KotlinLogging.logger {}

    @Transactional
    override fun processFile(msg: DatasetToProcessMessage) {
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

        distFromMahsRepository.saveAll(records)

        val processedDataset = ProcessedDataset(
            datasetType = msg.datasetType,
            year = msg.year,
            month = msg.month ?: 0
        )
        processedDatasetRepository.save(processedDataset)

        logger.info { "Dataset ${msg.datasetType} for ${msg.year}-${msg.month} marked as processed." }
    }

    private fun parseCsv(csvBytes: ByteArray): List<DistFromMahs> {
        val charset = Charset.forName("Windows-1250")
        val text = InputStreamReader(ByteArrayInputStream(csvBytes), charset).readText()

        val lines = text.split("\r\n", "\n").filter { it.isNotBlank() }
        val records = mutableListOf<DistFromMahs>()
        val medicinalProducts = referenceDataProvider.getMedicinalProducts()

        lines.drop(1).forEachIndexed { index, line ->
            val cols = line.split(";").map { it.trim('"') }

            try {
                val periodParts = cols[0].split(".")
                val year = periodParts[0].toIntOrNull() ?: throw IllegalArgumentException("Invalid year in period")
                val month = periodParts[1].toIntOrNull() ?: throw IllegalArgumentException("Invalid month in period")

                val purchaserType = MahPurchaserType.fromInput(cols[1])
                    ?: throw IllegalArgumentException("Invalid purchaser type: ${cols[1]}")

                val suklCode = cols[3].padStart(7, '0')
                val medicinalProduct = medicinalProducts[suklCode]
                    ?: throw IllegalStateException("MPD not found for code $suklCode")

                val movementType = MovementType.fromInput(cols[8])
                    ?: throw IllegalArgumentException("Invalid movement type: ${cols[8]}")

                val packageCount = cols[9].replace(",", ".").toDoubleOrNull()?.toInt()
                    ?: throw IllegalArgumentException("Invalid package count: ${cols[9]}")

                val record = DistFromMahs(
                    year = year,
                    month = month,
                    purchaserType = purchaserType,
                    medicinalProduct = medicinalProduct,
                    movementType = movementType,
                    packageCount = packageCount
                )

                records.add(record)
            } catch (e: Exception) {
                logger.error { "Error parsing line ${index + 2}: ${e.message} | Raw data: $line" }
            }
        }

        return records
    }
}
