package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.domain.entity.ProcessedDataset
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.NewFileMessage
import cz.machovec.lekovyportal.processor.mdp.MpdAddictionCategoryProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdAdministrationRouteProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdAtcGroupProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdCompositionFlagProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdCountryProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdDispenseTypeProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdDopingCategoryProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdDosageFormProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdGovernmentRegulationCategoryProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdIndicationGroupProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdMeasurementUnitProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdPackageTypeProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdRegistrationProcessProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdRegistrationStatusProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdSourceProcessor
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.net.URL
import java.time.LocalDate
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
    private val processedDatasetRepository: ProcessedDatasetRepository
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
        val extractedFiles = extractFiles(fileBytes)

        val (validFrom, validTo) = determineValidityDates(msg.year, msg.month ?: 1, extractedFiles["dlp_platnost.csv"])

        extractedFiles.forEach { (fileName, content) ->
            when (fileName) {
                "dlp_indikacniskupiny.csv" -> mpdIndicationGroupProcessor.importData(content, validFrom, validTo)
                "dlp_doping.csv" -> mpdDopingCategoryProcessor.importData(content, validFrom, validTo)
                "dlp_jednotky.csv" -> mpdMeasurementUnitProcessor.importData(content, validFrom, validTo)
                "dlp_regproc.csv" -> mpdRegistrationProcessProcessor.importData(content, validFrom, validTo)
                "dlp_stavyreg.csv" -> mpdRegistrationStatusProcessor.importData(content, validFrom, validTo)
                "dlp_vydej.csv" -> mpdDispenseTypeProcessor.importData(content, validFrom, validTo)
                "dlp_zavislost.csv" -> mpdAddictionCategoryProcessor.importData(content, validFrom, validTo)
                "dlp_zdroje.csv" -> mpdSourceProcessor.importData(content, validFrom, validTo)
                "dlp_slozenipriznak.csv" -> mpdCompositionFlagProcessor.importData(content, validFrom, validTo)
                "dlp_narvla.csv" -> mpdGovernmentRegulationCategoryProcessor.importData(content, validFrom, validTo)
                "dlp_zeme.csv" -> mpdCountryProcessor.importData(content, validFrom, validTo)
                "dlp_obaly.csv" -> mpdPackageTypeProcessor.importData(content, validFrom, validTo)
                "dlp_cesty.csv" -> mpdAdministrationRouteProcessor.importData(content, validFrom, validTo)
                "dlp_formy.csv" -> mpdDosageFormProcessor.importData(content, validFrom, validTo)
                "dlp_atc.csv" -> mpdAtcGroupProcessor.importData(content, validFrom, validTo)
            }
        }

        processedDatasetRepository.save(
            ProcessedDataset(datasetType = msg.datasetType, year = msg.year, month = msg.month ?: 0)
        )

        logger.info { "Dataset ${msg.datasetType} for ${msg.year}-${msg.month} processed successfully." }
    }

    private fun extractFiles(fileBytes: ByteArray): Map<String, ByteArray> {
        val extractedFiles = mutableMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(fileBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".csv", ignoreCase = true)) {
                    extractedFiles[entry.name] = zis.readBytes()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return extractedFiles
    }

    private fun determineValidityDates(year: Int, month: Int, validityFile: ByteArray?): Pair<LocalDate, LocalDate?> {
        return if (validityFile != null) {
            val text = validityFile.decodeToString()
            val lines = text.split("\r\n", "\n").filter { it.isNotBlank() }
            val firstLine = lines.drop(1).firstOrNull()?.split(";")

            if (firstLine != null && firstLine.size >= 2) {
                val validFrom = LocalDate.parse(firstLine[0], DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                val validTo = firstLine[1].takeIf { it.isNotEmpty() }?.let {
                    LocalDate.parse(it, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                }
                Pair(validFrom, validTo)
            } else {
                fallbackValidityDates(year, month)
            }
        } else {
            fallbackValidityDates(year, month)
        }
    }

    private fun fallbackValidityDates(year: Int, month: Int): Pair<LocalDate, LocalDate?> {
        val validFrom = LocalDate.of(year, month, 1)
        return Pair(validFrom, null)
    }
}
