package cz.machovec.lekovyportal.importer.processing.distribution

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.ProcessedDataset
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.domain.repository.dist.DistExportFromDistributorsRepository
import cz.machovec.lekovyportal.domain.repository.dist.DistFromDistributorsRepository
import cz.machovec.lekovyportal.domain.repository.dist.DistFromMahsRepository
import cz.machovec.lekovyportal.domain.repository.dist.DistFromPharmaciesRepository
import cz.machovec.lekovyportal.importer.columns.distribution.DistDistributorCsvColumn
import cz.machovec.lekovyportal.importer.columns.distribution.DistDistributorExportCsvColumn
import cz.machovec.lekovyportal.importer.columns.distribution.DistMahCsvColumn
import cz.machovec.lekovyportal.importer.columns.distribution.DistPharmacyCsvColumn
import cz.machovec.lekovyportal.importer.common.CsvImporter
import cz.machovec.lekovyportal.importer.common.RemoteFileDownloader
import cz.machovec.lekovyportal.importer.mapper.distribution.DistExportFromDistributorsRowMapper
import cz.machovec.lekovyportal.importer.mapper.distribution.DistFromDistributorsRowMapper
import cz.machovec.lekovyportal.importer.mapper.distribution.DistFromMahsRowMapper
import cz.machovec.lekovyportal.importer.mapper.distribution.DistFromPharmaciesRowMapper
import cz.machovec.lekovyportal.importer.processing.DatasetProcessingEvaluator
import cz.machovec.lekovyportal.messaging.DatasetToProcessMessage
import cz.machovec.lekovyportal.processor.DatasetProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI

@Service
class DistributionBundleJob(
    private val mahRepo: DistFromMahsRepository,
    private val distRepo: DistFromDistributorsRepository,
    private val exportRepo: DistExportFromDistributorsRepository,
    private val pharmacyRepo: DistFromPharmaciesRepository,
    private val processedRepo: ProcessedDatasetRepository,
    private val distCsvExtractor: DistCsvExtractor,
    private val downloader: RemoteFileDownloader,
    private val refData: MpdReferenceDataProvider,
    private val datasetProcessingEvaluator: DatasetProcessingEvaluator
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
                    val specs = DistMahCsvColumn.entries.map { it.toSpec() }
                    val mapper = DistFromMahsRowMapper(refData)
                    val records = CsvImporter().import(csv, specs, mapper)
                    if (records.isEmpty()) {
                        logger.info { "No valid records for ${msg.datasetType} in ${msg.year}-$month" }
                        continue
                    }
                    mahRepo.saveAll(records)
                }

                DatasetType.DISTRIBUTIONS_FROM_DISTRIBUTORS -> {
                    val specs = DistDistributorCsvColumn.entries.map { it.toSpec() }
                    val mapper = DistFromDistributorsRowMapper(refData)
                    val records = CsvImporter().import(csv, specs, mapper)
                    if (records.isEmpty()) {
                        logger.info { "No valid records for ${msg.datasetType} in ${msg.year}-$month" }
                        continue
                    }
                    distRepo.saveAll(records)
                }

                DatasetType.DISTRIBUTIONS_EXPORT_FROM_DISTRIBUTORS -> {
                    val specs = DistDistributorExportCsvColumn.entries.map { it.toSpec() }
                    val mapper = DistExportFromDistributorsRowMapper(refData)
                    val records = CsvImporter().import(csv, specs, mapper)
                    if (records.isEmpty()) {
                        logger.info { "No valid records for ${msg.datasetType} in ${msg.year}-$month" }
                        continue
                    }
                    exportRepo.saveAll(records)
                }

                DatasetType.DISTRIBUTIONS_FROM_PHARMACIES -> {
                    val specs = DistPharmacyCsvColumn.entries.map { it.toSpec() }
                    val mapper = DistFromPharmaciesRowMapper(refData)
                    val records = CsvImporter().import(csv, specs, mapper)
                    if (records.isEmpty()) {
                        logger.info { "No valid records for ${msg.datasetType} in ${msg.year}-$month" }
                        continue
                    }
                    pharmacyRepo.saveAll(records)
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
}
