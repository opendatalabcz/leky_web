package cz.machovec.lekovyportal.processor

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import cz.machovec.lekovyportal.domain.entity.HasDistrictCode
import cz.machovec.lekovyportal.domain.entity.ProcessedDataset
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.NewFileMessage
import mu.KotlinLogging
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.net.URL
import java.time.LocalDate
import java.util.zip.ZipInputStream

private val logger = KotlinLogging.logger {}

abstract class BaseEreceptFileProcessor<T>(
    private val datasetType: DatasetType,
    private val processedDatasetRepository: ProcessedDatasetRepository,
    private val batchInsert: (List<T>) -> Unit,
    private val parseCsvRecord: (List<String>) -> CsvRecordResult<T>?,
    private val mergeByQuantity: (T, T) -> T,
    private val extractAggregationKey: (T) -> String
) : DatasetFileProcessor where T : HasDistrictCode {

    private val PRAGUE_CODE = "3100"

    protected open val aggregatePrague: Boolean = true
    private val pragueMap = mutableMapOf<String, T>()
    private val nonPrague = mutableListOf<T>()

    @Transactional
    override fun processFile(msg: NewFileMessage) {
        val year = msg.year
        val month = msg.month

        val monthsToProcess: Set<Int> = if (month != null) {
            val mpdDone = processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(DatasetType.MPD, year, month)
            val alreadyDone = processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(datasetType, year, month)

            if (!mpdDone) {
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

            val processedMpd = processedDatasetRepository
                .findAllByDatasetTypeAndYear(DatasetType.MPD, year)
                .map { it.month }
                .toSet()

            val processedThis = processedDatasetRepository
                .findAllByDatasetTypeAndYear(datasetType, year)
                .map { it.month }
                .toSet()

            val eligible = completedMonths
                .filter { it !in processedThis && it in processedMpd }
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

        val finalRecords = if (aggregatePrague) {
            nonPrague + pragueMap.values
        } else {
            parsedRecords
        }

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
        var skipped = 0

        dataLines.forEachIndexed { index, cols ->
            if (cols.size < 10) return@forEachIndexed

            val month = cols[3].toIntOrNull() ?: return@forEachIndexed
            if (month !in allowedMonths) return@forEachIndexed

            val record = parseCsvRecord(cols.toList())?.entity
            if (record == null) {
                skipped++
                if (skipped <= 5) {
                    logger.warn { "Skipped line $index – unable to parse entity" }
                }
                return@forEachIndexed
            }

            processedMonths += month
            val handled = handleParsedEntity(record)
            if (handled != null) parsed += handled
        }

        logger.info { "Parsed ${parsed.size + pragueMap.size} records. Skipped: $skipped" }
        return parsed to processedMonths
    }


    private fun handleParsedEntity(entity: T): T? {
        if (!aggregatePrague || entity.districtCode != PRAGUE_CODE) return entity

        val key = extractAggregationKey(entity)
        val existing = pragueMap[key]

        if (existing != null) {
            pragueMap[key] = mergeByQuantity(existing, entity)
        } else {
            pragueMap[key] = entity
        }

        return null
    }
}

data class CsvRecordResult<T>(val entity: T)
