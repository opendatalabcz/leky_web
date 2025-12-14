package cz.machovec.lekovyportal.processor.processing.mpd

import cz.machovec.lekovyportal.core.domain.dataset.FileType
import cz.machovec.lekovyportal.messaging.dto.DatasetToProcessMessage
import cz.machovec.lekovyportal.processor.evaluator.DatasetProcessingEvaluator
import cz.machovec.lekovyportal.processor.processing.DatasetProcessor
import cz.machovec.lekovyportal.processor.util.RemoteFileDownloader
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.YearMonth
import java.util.zip.ZipFile

@Service
class MpdProcessor(
    private val downloader: RemoteFileDownloader,
    private val csvExtractor: MpdCsvExtractor,
    private val monthProcessor: MpdMonthProcessor,
    private val datasetProcessingEvaluator: DatasetProcessingEvaluator
) : DatasetProcessor {

    private val logger = KotlinLogging.logger {}

    override fun processFile(msg: DatasetToProcessMessage) {
        if (msg.fileType != FileType.ZIP) {
            logger.warn { "Skipping non-ZIP file type: ${msg.fileType}" }
            return
        }

        try {
            if (msg.month != null) {
                processSingleMonth(msg)
            } else {
                processAnnualZip(msg)
            }
        } catch (e: Exception) {
            logger.error(e) { "Critical error processing file: ${msg.fileUrl}" }
        }
    }

    private fun processSingleMonth(msg: DatasetToProcessMessage) {
        val month = msg.month!!
        if (!datasetProcessingEvaluator.canProcessMonth(msg.datasetType, msg.year, month)) {
            return
        }

        val zipBytes = downloader.downloadFile(URI(msg.fileUrl))
            ?: return logger.error { "Failed to download ZIP ${msg.fileUrl}" }

        val csvMap = csvExtractor.extractMpdCsvFiles(zipBytes)

        monthProcessor.processMonth(
            YearMonth.of(msg.year, month),
            csvMap
        )
    }

    private fun processAnnualZip(msg: DatasetToProcessMessage) {
        val tempZip = Files.createTempFile("mpd-", ".zip")
        logger.info { "Downloading MPD annual ZIP to temp file: $tempZip" }

        try {
            // 1) Stáhneme ZIP jednou na disk
            downloader.openStream(URI(msg.fileUrl)).use { input ->
                Files.copy(input, tempZip, StandardCopyOption.REPLACE_EXISTING)
            }

            // 2) Otevřeme ZIP s random access
            ZipFile(tempZip.toFile()).use { zip ->

                // 3) Najdeme všechny měsíce v ZIPu
                val monthToEntry = zip.entries().asSequence()
                    .filter { !it.isDirectory && it.name.endsWith(".zip", ignoreCase = true) }
                    .mapNotNull { entry ->
                        val month = MpdCsvExtractor.extractMonthFromZipName(entry.name)
                        month?.let { it to entry }
                    }
                    .toMap()

                // 4) Seřadíme měsíce vzestupně
                val months = monthToEntry.keys.sorted()

                logger.info { "MPD annual ZIP contains months: $months" }

                // 5) Zpracujeme měsíc po měsíci
                for (month in months) {
                    if (!datasetProcessingEvaluator.canProcessMonth(msg.datasetType, msg.year, month)) {
                        logger.info { "Skipping MPD ${msg.year}-$month due to evaluator" }
                        continue
                    }

                    val entry = monthToEntry[month]
                        ?: continue

                    logger.info { "Processing MPD ${msg.year}-$month from ${entry.name}" }

                    zip.getInputStream(entry).use { monthZipStream ->
                        val monthZipBytes = monthZipStream.readBytes()
                        val csvMap = csvExtractor.extractMpdCsvFiles(monthZipBytes)

                        monthProcessor.processMonth(
                            YearMonth.of(msg.year, month),
                            csvMap
                        )
                    }
                }
            }
        } finally {
            // 6) Úklid
            runCatching {
                Files.deleteIfExists(tempZip)
                logger.info { "Deleted temp file $tempZip" }
            }.onFailure {
                logger.warn(it) { "Failed to delete temp file $tempZip" }
            }
        }
    }
}
