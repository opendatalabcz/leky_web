package cz.machovec.lekovyportal.processor.processing.distribution

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.core.domain.dataset.ProcessedDataset
import cz.machovec.lekovyportal.core.domain.distribution.DistExportFromDistributors
import cz.machovec.lekovyportal.core.domain.distribution.DistFromDistributors
import cz.machovec.lekovyportal.core.domain.distribution.DistFromMahs
import cz.machovec.lekovyportal.core.domain.distribution.DistFromPharmacies
import cz.machovec.lekovyportal.core.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.core.repository.distribution.DistExportFromDistributorsRepository
import cz.machovec.lekovyportal.core.repository.distribution.DistFromDistributorsRepository
import cz.machovec.lekovyportal.core.repository.distribution.DistFromMahsRepository
import cz.machovec.lekovyportal.core.repository.distribution.DistFromPharmaciesRepository
import cz.machovec.lekovyportal.processor.processing.CsvImporter
import cz.machovec.lekovyportal.processor.util.RemoteFileDownloader
import cz.machovec.lekovyportal.processor.mapper.distribution.DistDistributorCsvColumn
import cz.machovec.lekovyportal.processor.mapper.distribution.DistDistributorExportCsvColumn
import cz.machovec.lekovyportal.processor.mapper.distribution.DistExportFromDistributorsRowMapper
import cz.machovec.lekovyportal.processor.mapper.distribution.DistFromDistributorsRowMapper
import cz.machovec.lekovyportal.processor.mapper.distribution.DistFromMahsRowMapper
import cz.machovec.lekovyportal.processor.mapper.distribution.DistFromPharmaciesRowMapper
import cz.machovec.lekovyportal.processor.mapper.distribution.DistMahCsvColumn
import cz.machovec.lekovyportal.processor.mapper.distribution.DistPharmacyCsvColumn
import cz.machovec.lekovyportal.processor.mapper.toSpec
import cz.machovec.lekovyportal.processor.evaluator.DatasetProcessingEvaluator
import cz.machovec.lekovyportal.messaging.dto.DatasetToProcessMessage
import cz.machovec.lekovyportal.processor.mapper.MutableImportStats
import cz.machovec.lekovyportal.processor.processing.DatasetProcessor
import cz.machovec.lekovyportal.processor.processing.mpd.MpdReferenceDataProvider
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.nio.charset.Charset

@Service
class DistributionProcessor(
    private val mahRepo: DistFromMahsRepository,
    private val distRepo: DistFromDistributorsRepository,
    private val exportRepo: DistExportFromDistributorsRepository,
    private val pharmacyRepo: DistFromPharmaciesRepository,
    private val processedRepo: ProcessedDatasetRepository,
    private val distCsvExtractor: DistCsvExtractor,
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
    }

    @Transactional
    override fun processFile(msg: DatasetToProcessMessage) {

        // Step 1 – Download file from the remote source
        val fileBytes = downloader.downloadFile(URI(msg.fileUrl))
            ?: return logger.error { "Download failed: ${msg.fileUrl}" }

        // Step 2 – Extract CSV files (per month)
        val csvByMonth = distCsvExtractor.extractCsvFilesByMonth(fileBytes, msg.month, msg.fileType, msg.datasetType)

        // Step 3 – Parse & persist per month
        for ((month, csvBytes) in csvByMonth) {
            if (!datasetProcessingEvaluator.canProcessMonth(msg.datasetType, msg.year, month)) {
                continue
            }

            if (msg.datasetType !in setOf(
                    DatasetType.DISTRIBUTIONS_FROM_MAHS,
                    DatasetType.DISTRIBUTIONS_FROM_DISTRIBUTORS,
                    DatasetType.DISTRIBUTIONS_EXPORT_FROM_DISTRIBUTORS,
                    DatasetType.DISTRIBUTIONS_FROM_PHARMACIES
                )
            ) {
                logger.error { "Unsupported dataset type ${msg.datasetType}" }
                continue
            }

            val importStats = MutableImportStats()

            csvBytes.inputStream().reader(CHARSET).use { reader ->
                when (msg.datasetType) {
                    DatasetType.DISTRIBUTIONS_FROM_MAHS -> {
                        val sequence = importer.importStream(
                            reader,
                            DistMahCsvColumn.entries.map { it.toSpec() },
                            DistFromMahsRowMapper(refData),
                            CSV_DATA_SEPARATOR,
                            importStats
                        )
                        persistMahs(sequence)
                    }

                    DatasetType.DISTRIBUTIONS_FROM_DISTRIBUTORS -> {
                        val sequence = importer.importStream(
                            reader,
                            DistDistributorCsvColumn.entries.map { it.toSpec() },
                            DistFromDistributorsRowMapper(refData),
                            CSV_DATA_SEPARATOR,
                            importStats
                        )
                        persistDistributors(sequence)
                    }

                    DatasetType.DISTRIBUTIONS_EXPORT_FROM_DISTRIBUTORS -> {
                        val sequence = importer.importStream(
                            reader,
                            DistDistributorExportCsvColumn.entries.map { it.toSpec() },
                            DistExportFromDistributorsRowMapper(refData),
                            CSV_DATA_SEPARATOR,
                            importStats
                        )
                        persistExports(sequence)
                    }

                    DatasetType.DISTRIBUTIONS_FROM_PHARMACIES -> {
                        val sequence = importer.importStream(
                            reader,
                            DistPharmacyCsvColumn.entries.map { it.toSpec() },
                            DistFromPharmaciesRowMapper(refData),
                            CSV_DATA_SEPARATOR,
                            importStats
                        )
                        persistPharmacies(sequence)
                    }

                    else -> error("Unsupported dataset type in DistributionProcessor: ${msg.datasetType}")
                }
            }

            logDistributionImport(msg.datasetType, msg.year, month, importStats)

            processedRepo.save(
                ProcessedDataset(
                    datasetType = msg.datasetType,
                    year = msg.year,
                    month = month
                )
            )

            logger.info {
                "Dataset ${msg.datasetType} for ${msg.year}-$month marked as processed."
            }
        }

        logger.info { "Dataset ${msg.datasetType} ${msg.year} - processFile COMPLETED" }
    }

    /** PERSIST HELPERS **/

    private fun persistMahs(sequence: Sequence<DistFromMahs>) {
        persistBatched(
            sequence,
            mahRepo::batchInsert
        )
    }

    private fun persistDistributors(sequence: Sequence<DistFromDistributors>) {
        persistBatched(
            sequence,
            distRepo::batchInsert
        )
    }

    private fun persistExports(sequence: Sequence<DistExportFromDistributors>) {
        persistBatched(
            sequence,
            exportRepo::batchInsert
        )
    }

    private fun persistPharmacies(sequence: Sequence<DistFromPharmacies>) {
        persistBatched(
            sequence,
            pharmacyRepo::batchInsert
        )
    }

    private fun <T> persistBatched(
        sequence: Sequence<T>,
        save: (List<T>) -> Unit
    ) {
        val buffer = mutableListOf<T>()

        for (item in sequence) {
            buffer += item
            if (buffer.size >= BATCH_SIZE) {
                save(buffer)
                buffer.clear()
            }
        }

        if (buffer.isNotEmpty()) {
            save(buffer)
        }
    }

    /** LOGGING **/

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
