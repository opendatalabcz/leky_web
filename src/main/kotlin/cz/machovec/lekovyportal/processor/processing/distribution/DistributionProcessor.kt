package cz.machovec.lekovyportal.processor.processing.distribution

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.core.domain.dataset.ProcessedDataset
import cz.machovec.lekovyportal.core.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.core.repository.distribution.DistExportFromDistributorsRepository
import cz.machovec.lekovyportal.core.repository.distribution.DistFromDistributorsRepository
import cz.machovec.lekovyportal.core.repository.distribution.DistFromMahsRepository
import cz.machovec.lekovyportal.core.repository.distribution.DistFromPharmaciesRepository
import cz.machovec.lekovyportal.processor.processing.CsvImporter
import cz.machovec.lekovyportal.processor.util.RemoteFileDownloader
import cz.machovec.lekovyportal.processor.mapper.DataImportResult
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
import cz.machovec.lekovyportal.processor.processing.DatasetProcessor
import cz.machovec.lekovyportal.processor.processing.mpd.MpdReferenceDataProvider
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI

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

    @Transactional
    override fun processFile(msg: DatasetToProcessMessage) {
        // Step 1 – Download file from the remote source
        val fileBytes = downloader.downloadFile(URI(msg.fileUrl))
            ?: return logger.error { "Download failed: ${msg.fileUrl}" }

        // Step 2 – Extract CSV files (per month)
        val csvByMonth = distCsvExtractor.extractCsvFilesByMonth(fileBytes, msg.month, msg.fileType, msg.datasetType)

        // Step 3 – Parse & persist per month
        for ((month, csv) in csvByMonth) {
            if (!datasetProcessingEvaluator.canProcessMonth(msg.datasetType, msg.year, month)) continue

            when (msg.datasetType) {
                DatasetType.DISTRIBUTIONS_FROM_MAHS -> {
                    val importResult = importer.import(
                        csv,
                        DistMahCsvColumn.entries.map { it.toSpec() },
                        DistFromMahsRowMapper(refData)
                    )
                    logImportSummary(msg.datasetType, importResult, msg.year, month)

                    if (importResult.successes.isEmpty()) continue
                    mahRepo.saveAll(importResult.successes)
                }

                DatasetType.DISTRIBUTIONS_FROM_DISTRIBUTORS -> {
                    val importResult = importer.import(
                        csv,
                        DistDistributorCsvColumn.entries.map { it.toSpec() },
                        DistFromDistributorsRowMapper(refData)
                    )
                    logImportSummary(msg.datasetType, importResult, msg.year, month)

                    if (importResult.successes.isEmpty()) continue
                    distRepo.saveAll(importResult.successes)
                }

                DatasetType.DISTRIBUTIONS_EXPORT_FROM_DISTRIBUTORS -> {
                    val importResult = importer.import(
                        csv,
                        DistDistributorExportCsvColumn.entries.map { it.toSpec() },
                        DistExportFromDistributorsRowMapper(refData)
                    )
                    logImportSummary(msg.datasetType, importResult, msg.year, month)

                    if (importResult.successes.isEmpty()) continue
                    exportRepo.saveAll(importResult.successes)
                }

                DatasetType.DISTRIBUTIONS_FROM_PHARMACIES -> {
                    val importResult = importer.import(
                        csv,
                        DistPharmacyCsvColumn.entries.map { it.toSpec() },
                        DistFromPharmaciesRowMapper(refData)
                    )
                    logImportSummary(msg.datasetType, importResult, msg.year, month)

                    if (importResult.successes.isEmpty()) continue
                    pharmacyRepo.saveAll(importResult.successes)
                }

                else -> {
                    logger.error { "Unsupported dataset type ${msg.datasetType}" }
                    continue
                }
            }

            processedRepo.save(
                ProcessedDataset(
                    datasetType = msg.datasetType,
                    year = msg.year,
                    month = month
                )
            )

            logger.info { "Dataset ${msg.datasetType} for ${msg.year}-$month marked as processed." }
        }
    }

    private fun <T> logImportSummary(datasetType: DatasetType, result: DataImportResult<T>, year: Int, month: Int) {
        if (result.failures.isEmpty()) {
            logger.info { "Import of $datasetType completed successfully for $year-$month (${result.successes.size}/${result.totalRows} rows)." }
            return
        }

        logger.warn {
            val reasonSummary = result.failuresByReason()
                .entries
                .joinToString { "${it.key}: ${it.value}" }

            val detailedSummary = result.failuresByReasonAndColumn()
                .entries
                .joinToString { (reasonAndColumn, count) ->
                    val (reason, column) = reasonAndColumn
                    "$reason in column '$column': $count"
                }

            """
        Import of $datasetType for $year-$month completed with errors:
          - Success: ${result.successes.size}/${result.totalRows}
          - Failures by reason: $reasonSummary
          - Failures by reason and column:
            $detailedSummary
        """.trimIndent()
        }
    }
}
