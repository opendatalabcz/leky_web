package cz.machovec.lekovyportal.processor.processing.distribution

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.core.domain.dataset.FileType
import cz.machovec.lekovyportal.core.domain.dataset.ProcessedDataset
import cz.machovec.lekovyportal.core.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.core.repository.distribution.DistExportFromDistributorsRepository
import cz.machovec.lekovyportal.core.repository.distribution.DistFromDistributorsRepository
import cz.machovec.lekovyportal.core.repository.distribution.DistFromMahsRepository
import cz.machovec.lekovyportal.core.repository.distribution.DistFromPharmaciesRepository
import cz.machovec.lekovyportal.messaging.dto.DatasetToProcessMessage
import cz.machovec.lekovyportal.processor.evaluator.DatasetProcessingEvaluator
import cz.machovec.lekovyportal.processor.mapper.MutableImportStats
import cz.machovec.lekovyportal.processor.mapper.distribution.DistDistributorCsvColumn
import cz.machovec.lekovyportal.processor.mapper.distribution.DistDistributorExportCsvColumn
import cz.machovec.lekovyportal.processor.mapper.distribution.DistExportFromDistributorsRowMapper
import cz.machovec.lekovyportal.processor.mapper.distribution.DistFromDistributorsRowMapper
import cz.machovec.lekovyportal.processor.mapper.distribution.DistFromMahsRowMapper
import cz.machovec.lekovyportal.processor.mapper.distribution.DistFromPharmaciesRowMapper
import cz.machovec.lekovyportal.processor.mapper.distribution.DistMahCsvColumn
import cz.machovec.lekovyportal.processor.mapper.distribution.DistPharmacyCsvColumn
import cz.machovec.lekovyportal.processor.mapper.toSpec
import cz.machovec.lekovyportal.processor.processing.CsvImporter
import cz.machovec.lekovyportal.processor.processing.DatasetProcessor
import cz.machovec.lekovyportal.processor.processing.mpd.MpdReferenceDataProvider
import cz.machovec.lekovyportal.processor.util.RemoteFileDownloader
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.FilterReader
import java.io.InputStreamReader
import java.io.Reader
import java.net.URI
import java.nio.charset.Charset
import java.util.zip.ZipInputStream

@Service
class DistributionProcessor(
    private val mahRepo: DistFromMahsRepository,
    private val distRepo: DistFromDistributorsRepository,
    private val exportRepo: DistExportFromDistributorsRepository,
    private val pharmacyRepo: DistFromPharmaciesRepository,
    private val processedRepo: ProcessedDatasetRepository,
    private val downloader: RemoteFileDownloader,
    private val refData: MpdReferenceDataProvider,
    private val datasetProcessingEvaluator: DatasetProcessingEvaluator,
    private val importer: CsvImporter
) : DatasetProcessor {

    private val logger = KotlinLogging.logger {}

    companion object {
        private val CHARSET = Charset.forName("Windows-1250")
        private const val CSV_DATA_SEPARATOR = ';'
        private const val BATCH_SIZE = 5_000
        private val PHARMACY_CSV_FILENAME_MONTH_REGEX = Regex("""LEK13_\d{4}(\d{2})v\d+\.csv""")
        private val ZIP_SUPPORTED_DATASETS = setOf(DatasetType.DISTRIBUTIONS_FROM_PHARMACIES)
    }

    @Transactional
    override fun processFile(msg: DatasetToProcessMessage) {

        /* -------------------------------------------------
         * STEP 0 – Basic message validation
         * ------------------------------------------------- */

        require(!(msg.fileType == FileType.ZIP && msg.month != null)) {
            "ZIP dataset ${msg.datasetType} must not specify month (month=${msg.month})"
        }

        require(!(msg.fileType == FileType.CSV && msg.month == null)) {
            "CSV dataset ${msg.datasetType} requires explicit month in message."
        }

        require(!(msg.fileType == FileType.ZIP && msg.datasetType !in ZIP_SUPPORTED_DATASETS)) {
            "DatasetType ${msg.datasetType} does not support ZIP processing."
        }

        /* -------------------------------------------------
         * STEP 1 – Download source file
         * ------------------------------------------------- */
        val fileBytes = downloader.downloadFile(URI(msg.fileUrl))
            ?: return logger.error { "Download failed: ${msg.fileUrl}" }

        /* -------------------------------------------------
         * STEP 2 – Dispatch by file type
         * ------------------------------------------------- */
        when (msg.fileType) {
            FileType.CSV -> processCsvDataset(fileBytes, msg)
            FileType.ZIP -> processZipDataset(fileBytes, msg)
        }
    }

    /* =================================================
     * CSV DATASET
     * ================================================= */

    private fun processCsvDataset(
        fileBytes: ByteArray,
        msg: DatasetToProcessMessage
    ) {
        val month = msg.month!!  // validated in STEP 0

        ByteArrayInputStream(fileBytes).use { input ->
            input.reader(CHARSET).use { reader ->
                processSingleMonth(
                    reader = reader,
                    datasetType = msg.datasetType,
                    year = msg.year,
                    month = month
                )
            }
        }
    }

    /* =================================================
     * ZIP DATASET (LEK13)
     * ================================================= */

    private fun processZipDataset(
        fileBytes: ByteArray,
        msg: DatasetToProcessMessage
    ) {
        ByteArrayInputStream(fileBytes).use { byteStream ->
            ZipInputStream(byteStream).use { zipStream ->

                var entry = zipStream.nextEntry
                while (entry != null) {

                    if (!entry.isDirectory) {
                        val month = extractMonthFromFileName(entry.name)

                        if (month != null) {
                            val reader = BufferedReader(
                                object : FilterReader(InputStreamReader(zipStream, CHARSET)) {
                                    override fun close() {
                                        // Prevent ZipInputStream from closing
                                    }
                                }
                            )

                            processSingleMonth(
                                reader = reader,
                                datasetType = msg.datasetType,
                                year = msg.year,
                                month = month
                            )
                        }
                    }

                    entry = zipStream.nextEntry
                }
            }
        }
    }

    /* =================================================
     * STEP 3 & 4 – Process single logical month
     * STEP 5 - Mark dataset as processed
     * ================================================= */

    private fun processSingleMonth(
        reader: Reader,
        datasetType: DatasetType,
        year: Int,
        month: Int
    ) {
        /* STEP 3 – Evaluator check */
        if (!datasetProcessingEvaluator.canProcessMonth(datasetType, year, month)) {
            return
        }

        logger.info { "Processing $datasetType $year-$month" }

        val importStats = MutableImportStats()

        /* STEP 4 – Import & persist data */
        when (datasetType) {

            DatasetType.DISTRIBUTIONS_FROM_MAHS -> {
                val seq = importer.importStream(
                    reader,
                    DistMahCsvColumn.entries.map { it.toSpec() },
                    DistFromMahsRowMapper(refData),
                    CSV_DATA_SEPARATOR,
                    importStats
                )
                persistBatched(seq, mahRepo::batchInsert)
            }

            DatasetType.DISTRIBUTIONS_FROM_DISTRIBUTORS -> {
                val seq = importer.importStream(
                    reader,
                    DistDistributorCsvColumn.entries.map { it.toSpec() },
                    DistFromDistributorsRowMapper(refData),
                    CSV_DATA_SEPARATOR,
                    importStats
                )
                persistBatched(seq, distRepo::batchInsert)
            }

            DatasetType.DISTRIBUTIONS_EXPORT_FROM_DISTRIBUTORS -> {
                val seq = importer.importStream(
                    reader,
                    DistDistributorExportCsvColumn.entries.map { it.toSpec() },
                    DistExportFromDistributorsRowMapper(refData),
                    CSV_DATA_SEPARATOR,
                    importStats
                )
                persistBatched(seq, exportRepo::batchInsert)
            }

            DatasetType.DISTRIBUTIONS_FROM_PHARMACIES -> {
                val seq = importer.importStream(
                    reader,
                    DistPharmacyCsvColumn.entries.map { it.toSpec() },
                    DistFromPharmaciesRowMapper(refData),
                    CSV_DATA_SEPARATOR,
                    importStats
                )
                persistBatched(seq, pharmacyRepo::batchInsert)
            }

            else -> error("Unsupported dataset type $datasetType")
        }

        /* STEP 5 – Mark dataset as processed */
        if (importStats.successCount == 0L) {
            logger.warn {
                "No successful rows for $datasetType $year-$month – dataset NOT marked as processed."
            }
            logDistributionImport(datasetType, year, month, importStats)
            return
        }

        processedRepo.save(
            ProcessedDataset(
                datasetType = datasetType,
                year = year,
                month = month
            )
        )

        logDistributionImport(datasetType, year, month, importStats)
    }

    /* =================================================
     * Helpers
     * ================================================= */

    private fun extractMonthFromFileName(fileName: String): Int? =
        PHARMACY_CSV_FILENAME_MONTH_REGEX
            .matchEntire(fileName)
            ?.groups?.get(1)?.value
            ?.toIntOrNull()

    private fun <T> persistBatched(
        sequence: Sequence<T>,
        saveAction: (List<T>) -> Unit
    ) {
        val buffer = ArrayList<T>(BATCH_SIZE)

        for (item in sequence) {
            buffer.add(item)
            if (buffer.size >= BATCH_SIZE) {
                saveAction(buffer)
                buffer.clear()
            }
        }

        if (buffer.isNotEmpty()) {
            saveAction(buffer)
        }
    }

    /* =================================================
     * Logging
     * ================================================= */

    private fun logDistributionImport(
        datasetType: DatasetType,
        year: Int,
        month: Int,
        stats: MutableImportStats
    ) {
        val scope = "$year-$month"

        if (stats.totalFailures == 0L) {
            logger.info {
                "IMPORT OK | $datasetType | $scope | " +
                        "success=${stats.successCount}/${stats.totalRows} | " +
                        "rate=${stats.successRatePercent()}%"
            }
            return
        }

        logger.warn {
            "IMPORT WARN | $datasetType | $scope | " +
                    "success=${stats.successCount}/${stats.totalRows} | " +
                    "rate=${stats.successRatePercent()}%"
        }

        stats.failuresByReason()
            .takeIf { it.isNotEmpty() }
            ?.let {
                logger.warn {
                    "IMPORT WARN | failures by reason: " +
                            it.entries.joinToString { e -> "${e.key}=${e.value}" }
                }
            }

        stats.failuresByReasonAndColumn()
            .takeIf { it.isNotEmpty() }
            ?.let {
                logger.warn {
                    "IMPORT WARN | failures by column: " +
                            it.entries.joinToString { e ->
                                val (_, column) = e.key
                                "${column ?: "?"}=${e.value}"
                            }
                }
            }
    }
}
