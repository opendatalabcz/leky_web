package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.domain.entity.EreceptPrescription
import cz.machovec.lekovyportal.domain.entity.FileType
import cz.machovec.lekovyportal.domain.entity.ProcessedDataset
import cz.machovec.lekovyportal.domain.repository.EreceptPrescriptionRepository
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.NewFileMessage
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.net.URL
import java.util.zip.ZipInputStream

private val logger = KotlinLogging.logger {}

@Service
class EreceptPrescriptionFileProcessor(
    private val ereceptPrescriptionRepository: EreceptPrescriptionRepository,
    private val processedDatasetRepository: ProcessedDatasetRepository,
) : DatasetFileProcessor {

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
        val records = if (msg.fileType == FileType.ZIP) {
            parseZip(fileBytes)
        } else {
            parseCsv(fileBytes)
        }

        logger.info("Processing file: ${msg.fileUrl}, records count: ${records.size}")

        ereceptPrescriptionRepository.batchInsert(records, batchSize = 50)

        val processedDataset = ProcessedDataset(
            datasetType = msg.datasetType,
            year = msg.year,
            month = msg.month ?: 0
        )
        processedDatasetRepository.save(processedDataset)

        logger.info { "Dataset ${msg.datasetType} for ${msg.year}-${msg.month} marked as processed." }
    }

    private fun parseZip(zipBytes: ByteArray): List<EreceptPrescription> {
        val result = mutableListOf<EreceptPrescription>()
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".csv", ignoreCase = true)) {
                    result += parseCsv(zis.readBytes())
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return result
    }

    private fun parseCsv(csvBytes: ByteArray): List<EreceptPrescription> {
        val text = csvBytes.decodeToString()
        val lines = text.split("\r\n", "\n").filter { it.isNotBlank() }

        return lines.drop(1).mapNotNull { line ->
            val cols = line.split(",").map { it.trim('"') }

            if (cols.size < 10) return@mapNotNull null

            val districtCode = cols[0]
            val year = cols[2].toIntOrNull() ?: return@mapNotNull null
            val month = cols[3].toIntOrNull() ?: return@mapNotNull null
            val suklCode = cols[4]
            val quantity = cols[9].toIntOrNull() ?: return@mapNotNull null

            EreceptPrescription(
                districtCode = districtCode,
                year = year,
                month = month,
                suklCode = suklCode,
                quantity = quantity
            )
        }
    }
}
