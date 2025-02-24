package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.domain.entity.EreceptDispense
import cz.machovec.lekovyportal.domain.entity.FileType
import cz.machovec.lekovyportal.domain.entity.ProcessedDataset
import cz.machovec.lekovyportal.domain.repository.EreceptDispenseRepository
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
class EreceptDispenseFileProcessor(
    private val ereceptDispenseRepository: EreceptDispenseRepository,
    private val processedDatasetRepository: ProcessedDatasetRepository,
) : DatasetFileProcessor {

    private val PRAGUE_DISTRICT_CODE = "3100"

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

        ereceptDispenseRepository.batchInsert(records, batchSize = 50)

        val processedDataset = ProcessedDataset(
            datasetType = msg.datasetType,
            year = msg.year,
            month = msg.month ?: 0
        )
        processedDatasetRepository.save(processedDataset)

        logger.info { "Dataset ${msg.datasetType} for ${msg.year}-${msg.month} marked as processed." }
    }

    private fun parseZip(zipBytes: ByteArray): List<EreceptDispense> {
        val result = mutableListOf<EreceptDispense>()
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

    private fun parseCsv(csvBytes: ByteArray): List<EreceptDispense> {
        val text = csvBytes.decodeToString()
        val lines = text.split("\r\n", "\n").filter { it.isNotBlank() }

        val pragueMap = mutableMapOf<String, EreceptDispense>()
        val otherRecords = mutableListOf<EreceptDispense>()

        lines.drop(1).forEach { line ->
            parseLine(line)?.let { dispense ->
                if (dispense.districtCode == PRAGUE_DISTRICT_CODE) {
                    groupPragueRecord(dispense, pragueMap)
                } else {
                    otherRecords.add(dispense)
                }
            }
        }

        return otherRecords + pragueMap.values
    }

    private fun parseLine(line: String): EreceptDispense? {
        val cols = line.split(",").map { it.trim('"') }
        if (cols.size < 10) return null

        val districtCode = cols[0]
        val year = cols[2].toIntOrNull() ?: return null
        val month = cols[3].toIntOrNull() ?: return null
        val suklCode = cols[4]
        val quantity = cols[9].toIntOrNull() ?: return null

        return EreceptDispense(
            districtCode = districtCode,
            year = year,
            month = month,
            suklCode = suklCode,
            quantity = quantity
        )
    }

    private fun groupPragueRecord(
        dispense: EreceptDispense,
        pragueMap: MutableMap<String, EreceptDispense>
    ) {
        val key = "${dispense.districtCode}-${dispense.year}-${dispense.month}-${dispense.suklCode}"
        val existing = pragueMap[key]

        if (existing != null) {
            pragueMap[key] = existing.copy(quantity = existing.quantity + dispense.quantity)
        } else {
            pragueMap[key] = dispense
        }
    }
}
