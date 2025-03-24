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

        logger.info("Saving records from file: ${msg.fileUrl}, records count: ${records.size}")
        ereceptPrescriptionRepository.batchInsert(records, batchSize = 50)

        val processedDataset = ProcessedDataset(
            datasetType = msg.datasetType,
            year = msg.year,
            month = msg.month ?: 0
        )
        processedDatasetRepository.saveAndFlush(processedDataset)

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
        val start = System.currentTimeMillis()

        val text = csvBytes.decodeToString()
        val lines = text.split("\r\n", "\n").filter { it.isNotBlank() }

        val pragueMap = mutableMapOf<String, EreceptPrescription>()
        val otherRecords = mutableListOf<EreceptPrescription>()
        val districtNames = mutableMapOf<String, String>()

        var skippedCount = 0

        logger.info { "Starting CSV parsing, total lines: ${lines.size - 1}" }

        lines.drop(1).forEachIndexed { index, line ->
            val cols = line.split(",").map { it.trim('"') }
            if (cols.size < 10) return@forEachIndexed

            val districtCode = cols[0]
            val districtName = cols[1]
            val year = cols[2].toIntOrNull() ?: return@forEachIndexed
            val month = cols[3].toIntOrNull() ?: return@forEachIndexed
            val suklCode = cols[4]
            val quantity = cols[9].toIntOrNull() ?: return@forEachIndexed

            districtNames[districtCode] = districtName

            val medicinalProduct = referenceDataProvider.getMedicinalProducts()[suklCode]

            if (medicinalProduct == null) {
                skippedCount++
                if (skippedCount <= 5) {
                    logger.warn { "Skipped line $index – unknown SUKL code: $suklCode ($districtCode $year-$month)" }
                }
                return@forEachIndexed
            }

            val prescription = EreceptPrescription(
                districtCode = districtCode,
                year = year,
                month = month,
                medicinalProduct = medicinalProduct,
                quantity = quantity
            )

            if (districtCode == PRAGUE_DISTRICT_CODE) {
                groupPragueRecord(prescription, pragueMap, suklCode)
            } else {
                otherRecords.add(prescription)
            }

            if ((index + 1) % 10_000 == 0) {
                logger.info { "Parsed ${index + 1} lines so far..." }
            }
        }

        val totalParsed = otherRecords.size + pragueMap.size
        val duration = System.currentTimeMillis() - start

        logger.info {
            "Finished parsing. Valid records: $totalParsed, Skipped: $skippedCount, Time: ${duration}ms"
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
