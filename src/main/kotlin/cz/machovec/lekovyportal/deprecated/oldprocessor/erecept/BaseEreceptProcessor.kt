package cz.machovec.lekovyportal.deprecated.oldprocessor.erecept

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.core.domain.dataset.FileType
import cz.machovec.lekovyportal.deprecated.domain.HasDistrictCode
import cz.machovec.lekovyportal.core.domain.dataset.ProcessedDataset
import cz.machovec.lekovyportal.core.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.dto.DatasetToProcessMessage
import cz.machovec.lekovyportal.processor.processing.DatasetProcessor
import mu.KotlinLogging
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.net.URL
import java.time.LocalDate
import java.util.zip.ZipInputStream

private val logger = KotlinLogging.logger {}

abstract class BaseEreceptProcessor<T>(
    private val datasetType: DatasetType,
    private val processedDatasetRepository: ProcessedDatasetRepository,
    private val batchInsert: (List<T>) -> Unit,
    private val parseCsvRecord: (List<String>) -> CsvRecordResult<T>?,
    private val mergeByQuantity: (T, T) -> T,
    private val extractAggregationKey: (T) -> String
) : DatasetProcessor where T : HasDistrictCode {

    private val PRAGUE_CODE = "3100"

    protected open val aggregatePrague: Boolean = true

    @Transactional
    override fun processFile(msg: DatasetToProcessMessage) {
        val year = msg.year
        val month = msg.month

        val monthsToProcess: Set<Int> = if (month != null) {
            val MEDICINALPRODUCTDATABASEDone = processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(DatasetType.MEDICINAL_PRODUCT_DATABASE, year, month)
            val alreadyDone = processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(datasetType, year, month)

            if (!MEDICINALPRODUCTDATABASEDone) {
                logger.warn { "MPD not processed for $year-$month → skipping $datasetType" }
                return
            }
            if (alreadyDone) {
                logger.info { "$datasetType for $year-$month already processed → skipping." }
                return
            }

            setOf(month)
        } else {
            val now = LocalDate.now()
            val completedMonths = if (year < now.year) (1..12).toSet()
            else if (year == now.year) (1 until now.monthValue).toSet()
            else emptySet()

            val processedMEDICINALPRODUCTDATABASE = processedDatasetRepository
                .findAllByDatasetTypeAndYear(DatasetType.MEDICINAL_PRODUCT_DATABASE, year)
                .map { it.month }
                .toSet()

            val processedThis = processedDatasetRepository
                .findAllByDatasetTypeAndYear(datasetType, year)
                .map { it.month }
                .toSet()

            val eligible = completedMonths
                .filter { it !in processedThis && it in processedMEDICINALPRODUCTDATABASE }
                .toSet()

            if (eligible.isEmpty()) {
                logger.info { "No eligible months for $datasetType in $year. Skipping." }
                return
            }

            logger.info { "Will process months $eligible from yearly dataset $year" }
            eligible
        }

        val fileBytes = URL(msg.fileUrl).readBytes()
        val (parsedRecords, parsedMonths) = if (msg.fileType == FileType.ZIP) {
            parseZip(fileBytes, monthsToProcess)
        } else {
            parseCsv(fileBytes, monthsToProcess)
        }

        val finalRecords = parsedRecords

        if (finalRecords.isEmpty()) {
            logger.info { "No records to save for $datasetType." }
            return
        }

        logger.info { "Saving ${finalRecords.size} records to DB for $datasetType, months: ${parsedMonths.sorted()}" }
        batchInsert(finalRecords)

        parsedMonths.forEach { m ->
            processedDatasetRepository.save(
                ProcessedDataset(
                    datasetType = datasetType,
                    year = msg.year,
                    month = m
                )
            )
        }

        logger.info { "Finished processing $datasetType for year ${msg.year}, months: ${parsedMonths.sorted()}" }
    }

    private fun parseZip(zipBytes: ByteArray, allowedMonths: Set<Int>): Pair<List<T>, Set<Int>> {
        val result = mutableListOf<T>()
        val processedMonths = mutableSetOf<Int>()

        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".csv", ignoreCase = true)) {
                    val (parsed, months) = parseCsv(zis.readBytes(), allowedMonths)
                    result += parsed
                    processedMonths += months
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        return result to processedMonths
    }

    private fun parseCsv(csvBytes: ByteArray, allowedMonths: Set<Int>): Pair<List<T>, Set<Int>> {
        val reader = CSVReaderBuilder(csvBytes.inputStream().reader(Charsets.UTF_8))
            .withCSVParser(CSVParserBuilder().withSeparator(',').build())
            .build()

        val lines = reader.readAll()
        if (lines.isEmpty()) return emptyList<T>() to emptySet()

        val header = lines.first()
        val dataLines = lines.drop(1)

        val processedMonths = mutableSetOf<Int>()
        val parsed = mutableListOf<T>()
        val pragueMap = mutableMapOf<String, T>()
        var skipped = 0

        dataLines.forEachIndexed { index, cols ->
            if (cols.size < 10) return@forEachIndexed

            val month = cols[3].toIntOrNull() ?: return@forEachIndexed
            if (month !in allowedMonths) return@forEachIndexed

            val result = parseCsvRecord(cols.toList()) ?: return@forEachIndexed
            val entity = result.entity

            processedMonths += month

            if (aggregatePrague && entity.districtCode == PRAGUE_CODE) {
                val key = extractAggregationKey(entity)
                val existing = pragueMap[key]

                if (existing != null) {
                    pragueMap[key] = mergeByQuantity(existing, entity)
                } else {
                    pragueMap[key] = entity
                }
            } else {
                parsed += entity
            }
        }

        val combined = parsed + pragueMap.values
        logger.info { "Parsed ${combined.size} records. Skipped: $skipped" }
        return combined to processedMonths
    }
}

data class CsvRecordResult<T>(val entity: T)
