package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.domain.entity.EreceptPrescription
import cz.machovec.lekovyportal.domain.entity.FileType
import cz.machovec.lekovyportal.domain.entity.ProcessedDataset
import cz.machovec.lekovyportal.domain.repository.EreceptPrescriptionRepository
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.NewFileMessage
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider
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
    private val referenceDataProvider: MpdReferenceDataProvider
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
        val (records, districtNames) = if (msg.fileType == FileType.ZIP) {
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

    private fun parseZip(zipBytes: ByteArray): Pair<List<EreceptPrescription>, Map<String, String>> {
        val result = mutableListOf<EreceptPrescription>()
        val districtNames = mutableMapOf<String, String>()

        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".csv", ignoreCase = true)) {
                    val (parsedRecords, parsedDistricts) = parseCsv(zis.readBytes())
                    result += parsedRecords
                    districtNames.putAll(parsedDistricts)
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return Pair(result, districtNames)
    }

    private fun parseCsv(csvBytes: ByteArray): Pair<List<EreceptPrescription>, Map<String, String>> {
        val text = csvBytes.decodeToString()
        val lines = text.split("\r\n", "\n").filter { it.isNotBlank() }

        val pragueMap = mutableMapOf<String, EreceptPrescription>()
        val otherRecords = mutableListOf<EreceptPrescription>()
        val districtNames = mutableMapOf<String, String>()

        lines.drop(1).forEach { line ->
            val cols = line.split(",").map { it.trim('"') }
            if (cols.size < 10) return@forEach

            val districtCode = cols[0]
            val districtName = cols[1] // Název okresu
            val year = cols[2].toIntOrNull() ?: return@forEach
            val month = cols[3].toIntOrNull() ?: return@forEach
            val suklCode = cols[4]
            val quantity = cols[9].toIntOrNull() ?: return@forEach

            districtNames[districtCode] = districtName

            val medicinalProduct = referenceDataProvider.getMedicinalProducts()[suklCode]

            val prescription = EreceptPrescription(
                districtCode = districtCode,
                year = year,
                month = month,
                medicinalProduct = medicinalProduct!!,
                quantity = quantity
            )

            if (districtCode == PRAGUE_DISTRICT_CODE) {
                groupPragueRecord(prescription, pragueMap, suklCode)
            } else {
                otherRecords.add(prescription)
            }
        }

        return Pair(otherRecords + pragueMap.values, districtNames)
    }

    private fun groupPragueRecord(
        prescription: EreceptPrescription,
        pragueMap: MutableMap<String, EreceptPrescription>,
        suklCode: String
    ) {
        val key = "${prescription.districtCode}-${prescription.year}-${prescription.month}-${suklCode}"
        val existing = pragueMap[key]

        if (existing != null) {
            pragueMap[key] = existing.copy(quantity = existing.quantity + prescription.quantity)
        } else {
            pragueMap[key] = prescription
        }
    }
}
