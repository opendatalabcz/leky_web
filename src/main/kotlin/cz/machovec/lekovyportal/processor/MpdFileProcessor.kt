package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.domain.entity.ProcessedDataset
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.NewFileMessage
import cz.machovec.lekovyportal.processor.mdp.MpdActiveSubstanceProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdAddictionCategoryProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdAdministrationRouteProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdAtcGroupProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdCancelledRegistrationProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdCompositionFlagProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdCountryProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdDispenseTypeProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdDopingCategoryProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdDosageFormProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdGovernmentRegulationCategoryProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdIndicationGroupProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdMeasurementUnitProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdMedicinalProductProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdOrganisationProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdPackageTypeProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdRegistrationExceptionProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdRegistrationProcessProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdRegistrationStatusProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdSourceProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdSubstanceProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdSubstanceSynonymProcessor
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.net.URL
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream

private val logger = KotlinLogging.logger {}

@Service
class MpdFileProcessor(
    private val mpdIndicationGroupProcessor: MpdIndicationGroupProcessor,
    private val mpdDopingCategoryProcessor: MpdDopingCategoryProcessor,
    private val mpdMeasurementUnitProcessor: MpdMeasurementUnitProcessor,
    private val mpdRegistrationProcessProcessor: MpdRegistrationProcessProcessor,
    private val mpdRegistrationStatusProcessor: MpdRegistrationStatusProcessor,
    private val mpdDispenseTypeProcessor: MpdDispenseTypeProcessor,
    private val mpdAddictionCategoryProcessor: MpdAddictionCategoryProcessor,
    private val mpdSourceProcessor: MpdSourceProcessor,
    private val mpdCompositionFlagProcessor: MpdCompositionFlagProcessor,
    private val mpdGovernmentRegulationCategoryProcessor: MpdGovernmentRegulationCategoryProcessor,
    private val mpdCountryProcessor: MpdCountryProcessor,
    private val mpdPackageTypeProcessor: MpdPackageTypeProcessor,
    private val mpdAdministrationRouteProcessor: MpdAdministrationRouteProcessor,
    private val mpdDosageFormProcessor: MpdDosageFormProcessor,
    private val mpdAtcGroupProcessor: MpdAtcGroupProcessor,
    private val mpdOrganisationProcessor: MpdOrganisationProcessor,
    private val mpdActiveSubstanceProcessor: MpdActiveSubstanceProcessor,
    private val mpdSubstanceProcessor: MpdSubstanceProcessor,
    private val mpdSubstanceSynonymProcessor: MpdSubstanceSynonymProcessor,
    private val processedDatasetRepository: ProcessedDatasetRepository,
    private val mpdMedicinalProductProcessor: MpdMedicinalProductProcessor,
    private val mpdRegistrationExceptionProcessor: MpdRegistrationExceptionProcessor,
    private val mpdCancelledRegistrationProcessor: MpdCancelledRegistrationProcessor
) : DatasetFileProcessor {

    @Transactional
    override fun processFile(msg: NewFileMessage) {
        logger.info { "Starting processing of dataset=${msg.datasetType}, year=${msg.year}, month=${msg.month}, url=${msg.fileUrl}" }

        val fileBytes = URL(msg.fileUrl).readBytes()

        if (msg.month == null) {
            processYearlyZip(msg, fileBytes)
        } else {
            processMonthlyZip(msg, fileBytes)
        }
    }

    private fun processYearlyZip(msg: NewFileMessage, annualZipBytes: ByteArray) {
        logger.info { "Detected yearly ZIP. Extracting monthly zips..." }

        val monthlyZips = extractMonthlyZips(annualZipBytes)
            .toSortedMap(compareBy { parseMonthFromFilename(it) ?: 0 })

        monthlyZips.forEach { (monthFileName, zipContent) ->
            val month = parseMonthFromFilename(monthFileName)
            if (month == null) {
                logger.warn { "Skipping $monthFileName, cannot parse month from name." }
                return@forEach
            }

            val monthlyMsg = msg.copy(month = month)
            processMonthlyZip(monthlyMsg, zipContent)
        }

        logger.info { "Finished processing all monthly zips in annual zip for year=${msg.year}" }
    }

    private fun processMonthlyZip(msg: NewFileMessage, fileBytes: ByteArray) {
        logger.info { "Processing monthly ZIP for year=${msg.year}, month=${msg.month}" }

        if (msg.month == null) return

        if (msg.year != 2021 || msg.month != 1) {
            val previousYearMonth = YearMonth.of(msg.year, msg.month).minusMonths(1)
            val isPreviousProcessed = processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(
                msg.datasetType,
                previousYearMonth.year,
                previousYearMonth.monthValue
            )
            if (!isPreviousProcessed) {
                logger.warn { "Previous month ${previousYearMonth} not processed yet. Skipping ${msg.year}-${msg.month}." }
                return
            }
        }

        val isProcessed = processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(
            msg.datasetType,
            msg.year,
            msg.month
        )
        if (isProcessed) {
            logger.info { "Dataset ${msg.datasetType} for ${msg.year}-${msg.month} already processed. Skipping." }
            return
        }

        val extractedFiles = extractCsvFiles(fileBytes)
        val (validFrom, validTo) = determineValidityDates(msg.year, msg.month, extractedFiles["dlp_platnost.csv"])

        extractedFiles.forEach { (fileName, content) ->
            when (fileName) {
                "dlp_lecivepripravky.csv" -> mpdMedicinalProductProcessor.processCsv(content, validFrom, validTo)
            }
        }

        processedDatasetRepository.save(
            ProcessedDataset(
                datasetType = msg.datasetType,
                year = msg.year,
                month = msg.month
            )
        )

        logger.info { "Dataset ${msg.datasetType} for ${msg.year}-${msg.month} processed successfully." }
    }

    private fun extractMonthlyZips(annualZip: ByteArray): Map<String, ByteArray> {
        val result = mutableMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(annualZip)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".zip", ignoreCase = true)) {
                    result[entry.name] = zis.readBytes()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return result
    }

    private fun extractCsvFiles(zipBytes: ByteArray): Map<String, ByteArray> {
        val extractedFiles = mutableMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".csv", ignoreCase = true)) {
                    val fileName = entry.name.substringAfterLast('/')
                    extractedFiles[fileName] = zis.readBytes()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return extractedFiles
    }

    private fun parseMonthFromFilename(fileName: String): Int? {
        val regex = Regex(".*(\\d{4})(\\d{2}).*\\.zip")
        val match = regex.matchEntire(fileName)
        return match?.destructured?.component2()?.toIntOrNull()
    }

    private fun determineValidityDates(year: Int, month: Int, validityFile: ByteArray?): Pair<LocalDate, LocalDate> {
        return if (validityFile != null) {
            val text = validityFile.decodeToString()
            val lines = text.split("\r\n", "\n").filter { it.isNotBlank() }
            val firstLine = lines.drop(1).firstOrNull()?.split(";")

            if (firstLine != null && firstLine.size >= 2) {
                val validFrom = LocalDate.parse(firstLine[0], DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                val validTo = firstLine[1].takeIf { it.isNotEmpty() }?.let {
                    LocalDate.parse(it, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                } ?: YearMonth.of(year, month).atEndOfMonth()
                validFrom to validTo
            } else {
                fallbackValidityDates(year, month)
            }
        } else {
            fallbackValidityDates(year, month)
        }
    }

    private fun fallbackValidityDates(year: Int, month: Int): Pair<LocalDate, LocalDate> {
        val start = LocalDate.of(year, month, 1)
        val end = YearMonth.of(year, month).atEndOfMonth()
        return start to end
    }
}
