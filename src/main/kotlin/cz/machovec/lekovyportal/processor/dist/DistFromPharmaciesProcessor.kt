package cz.machovec.lekovyportal.processor.dist

import cz.machovec.lekovyportal.domain.entity.FileType
import cz.machovec.lekovyportal.domain.entity.distribution.PharmacyDispenseType
import cz.machovec.lekovyportal.domain.entity.distribution.DistFromPharmacies
import cz.machovec.lekovyportal.domain.entity.ProcessedDataset
import cz.machovec.lekovyportal.domain.repository.dist.DistFromPharmaciesRepository
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
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
import java.util.zip.ZipInputStream

@Service
class DistFromPharmaciesProcessor(
    private val distFromPharmaciesRepository: DistFromPharmaciesRepository,
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
        val records = when (msg.fileType) {
            FileType.ZIP -> {
                logger.info { "Detected ZIP file for ${msg.datasetType}. Parsing all CSV inside..." }
                parseZip(fileBytes)
            }
            FileType.CSV -> {
                logger.info { "Detected CSV file for ${msg.datasetType}. Parsing directly..." }
                parseCsv(fileBytes)
            }
        }

        logger.info("Processing file: ${msg.fileUrl}, records count: ${records.size}")

        distFromPharmaciesRepository.saveAll(records)

        val processedDataset = ProcessedDataset(
            datasetType = msg.datasetType,
            year = msg.year,
            month = msg.month ?: 0
        )
        processedDatasetRepository.save(processedDataset)

        logger.info { "Dataset ${msg.datasetType} for ${msg.year}-${msg.month} marked as processed." }
    }

    private fun parseZip(zipBytes: ByteArray): List<DistFromPharmacies> {
        val records = mutableListOf<DistFromPharmacies>()

        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".csv", ignoreCase = true)) {
                    logger.info { "Extracting CSV file: ${entry.name}" }
                    val csvBytes = zis.readBytes()
                    records.addAll(parseCsv(csvBytes))
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return records
    }

    private fun parseCsv(csvBytes: ByteArray): List<DistFromPharmacies> {
        val charset = Charset.forName("Windows-1250")
        val text = InputStreamReader(ByteArrayInputStream(csvBytes), charset).readText()

        val lines = text.split("\r\n", "\n").filter { it.isNotBlank() }

        val records = mutableListOf<DistFromPharmacies>()

        lines.drop(1).forEachIndexed { index, line ->
            val cols = line.split(";").map { it.trim('"') }

            try {
                val periodParts = cols[0].split(".")
                val year = periodParts[0].toIntOrNull() ?: throw IllegalArgumentException("Invalid year in period")
                val month = periodParts[1].toIntOrNull() ?: throw IllegalArgumentException("Invalid month in period")

                val dispenseType = PharmacyDispenseType.fromInput(cols[1])
                    ?: throw IllegalArgumentException("Invalid purchaser type: ${cols[1]}")

                val suklCode = cols[3]

                val packageCount = cols[8]
                    .replace(",", ".")
                    .toBigDecimalOrNull()
                    ?: throw IllegalArgumentException("Invalid package count: ${cols[8]}")

                val medicinalProduct = referenceDataProvider.getMedicinalProducts()[suklCode]

                val record = DistFromPharmacies(
                    year = year,
                    month = month,
                    medicinalProduct = medicinalProduct!!,
                    dispenseType = dispenseType,
                    packageCount = packageCount,
                )

                records.add(record)
            } catch (e: Exception) {
                logger.error { "Error parsing line ${index + 2}: ${e.message} | Raw data: $line" }
            }
        }
        return records
    }
}
