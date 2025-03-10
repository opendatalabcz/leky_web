package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.domain.entity.ProcessedDataset
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.NewFileMessage
import cz.machovec.lekovyportal.processor.mdp.MpdDopingCategoryProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdIndicationGroupProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdMeasurementUnitProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdRegistrationProcessProcessor
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
