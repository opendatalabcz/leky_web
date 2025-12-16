package cz.machovec.lekovyportal.processor.processing.mpd

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
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
    private val monthProcessor: MpdMonthProcessor,
    private val datasetProcessingEvaluator: DatasetProcessingEvaluator
) : DatasetProcessor {

    private val logger = KotlinLogging.logger {}

    override fun processFile(msg: DatasetToProcessMessage) {

        /* -------------------------------------------------
         * STEP 0 – Basic message validation
         * ------------------------------------------------- */

        require(msg.fileType == FileType.ZIP) {
            "MPD dataset must be provided as ZIP (fileType=${msg.fileType})"
        }

        require(msg.datasetType == DatasetType.MEDICINAL_PRODUCT_DATABASE) {
            "Unsupported datasetType for MPD processor: ${msg.datasetType}"
        }

        /* -------------------------------------------------
         * STEP 1 – Dispatch by scope
         * ------------------------------------------------- */

        if (msg.month != null) {
            processSingleMonth(msg)
        } else {
            processAnnualZip(msg)
        }
    }

    /* =================================================
     * Single month ZIP
     * ================================================= */

    private fun processSingleMonth(msg: DatasetToProcessMessage) {
        val month = msg.month!!

        /* -------------------------------------------------
         * STEP 2 – Evaluator
         * ------------------------------------------------- */
        if (!datasetProcessingEvaluator.canProcessMonth(
                DatasetType.MEDICINAL_PRODUCT_DATABASE,
                msg.year,
                month
            )
        ) {
            return
        }

        /* -------------------------------------------------
         * STEP 3 – Download & process ZIP (stream → byte[])
         * ------------------------------------------------- */

        val zipBytes = downloader.downloadFile(URI(msg.fileUrl))
            ?: return logger.error { "Failed to download ZIP ${msg.fileUrl}" }

        val csvMap = MpdCsvExtractor.extractMpdCsvFiles(zipBytes)

        monthProcessor.processMonth(
            YearMonth.of(msg.year, month),
            csvMap
        )
    }

    /* =================================================
     * Annual ZIP (streamed)
     * ================================================= */

    private fun processAnnualZip(msg: DatasetToProcessMessage) {
        val tempZip = Files.createTempFile("mpd-", ".zip")

        try {
            /* -------------------------------------------------
             * STEP 2 – Download ZIP STREAM → disk
             * ------------------------------------------------- */

            downloader.openStream(URI(msg.fileUrl)).use { input ->
                Files.copy(input, tempZip, StandardCopyOption.REPLACE_EXISTING)
            }

            /* -------------------------------------------------
             * STEP 3 – Random access over annual ZIP
             * ------------------------------------------------- */

            ZipFile(tempZip.toFile()).use { zip ->
                val monthToEntry = zip.entries().asSequence()
                    .filter { !it.isDirectory && it.name.endsWith(".zip", ignoreCase = true) }
                    .mapNotNull { entry ->
                        MpdCsvExtractor.extractMonthFromZipName(entry.name)
                            ?.let { it to entry }
                    }
                    .toMap()

                for (month in monthToEntry.keys.sorted()) {
                    /* -------------------------------------------------
                     * STEP 4 – Evaluator
                     * ------------------------------------------------- */

                    if (!datasetProcessingEvaluator.canProcessMonth(
                            DatasetType.MEDICINAL_PRODUCT_DATABASE,
                            msg.year,
                            month
                        )
                    ) {
                        continue
                    }

                    val entry = monthToEntry[month] ?: continue

                    /* -------------------------------------------------
                     * STEP 5 – Process one month ZIP
                     * ------------------------------------------------- */

                    zip.getInputStream(entry).use { monthZipStream ->
                        val monthZipBytes = monthZipStream.readBytes()

                        val csvMap = MpdCsvExtractor.extractMpdCsvFiles(monthZipBytes)

                        monthProcessor.processMonth(
                            YearMonth.of(msg.year, month),
                            csvMap
                        )
                    }
                }
            }
        } finally {
            Files.deleteIfExists(tempZip)
        }
    }
}
