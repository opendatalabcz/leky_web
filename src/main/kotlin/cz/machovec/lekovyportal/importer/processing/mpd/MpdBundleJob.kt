package cz.machovec.lekovyportal.importer.processing.mpd

import cz.machovec.lekovyportal.MpdValidityReader
import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import cz.machovec.lekovyportal.domain.entity.ProcessedDataset
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.importer.common.RemoteFileDownloader
import cz.machovec.lekovyportal.importer.processing.DatasetProcessingEvaluator
import cz.machovec.lekovyportal.messaging.DatasetToProcessMessage
import cz.machovec.lekovyportal.processor.DatasetProcessor
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.time.LocalDate
import java.time.YearMonth

@Service
class MpdBundleJob(
    private val processedDatasetRepository: ProcessedDatasetRepository,
    private val tablesProcessor: MpdTablesProcessor,
    private val csvExtractor: MpdCsvExtractor,
    private val downloader: RemoteFileDownloader,
    private val validityReader: MpdValidityReader,
    private val datasetProcessingEvaluator: DatasetProcessingEvaluator,
    private val referenceDataProvider: MpdReferenceDataProvider
) : DatasetProcessor {

    private val logger = KotlinLogging.logger {}

    companion object {
        private val DATASET_TYPE = DatasetType.MEDICINAL_PRODUCT_DATABASE
        private val VALIDITY_REQUIRED_SINCE = YearMonth.of(2023, 4)
    }

    @Transactional
    override fun processFile(msg: DatasetToProcessMessage) {
        // Step 1 – Validate that the file type is ZIP before proceeding
        if (msg.fileType != FileType.ZIP) {
            logger.warn { "Skipping non-ZIP file type: ${msg.fileType} for dataset ${msg.datasetType}" }
            return
        }

        // Step 2 – Download the ZIP file from the remote source
        val zipBytes = downloader.downloadFile(URI(msg.fileUrl))
            ?: return logger.error { "Failed to download ZIP for ${msg.fileUrl}" }

        // Step 3 – Extract and group CSV files by month (handles both annual and monthly ZIPs)
        val csvFilesByMonth = csvExtractor.extractMonthlyCsvFilesFromZip(zipBytes, msg.month)

        // Step 4 - Process each CSV file
        for ((month, csvMap) in csvFilesByMonth) {
            if (!datasetProcessingEvaluator.canProcessMonth(msg.datasetType, msg.year, month)) continue

            val period = YearMonth.of(msg.year, month)
            processMonth(period, csvMap)
        }
    }

    private fun processMonth(monthToProcess: YearMonth, csvMap: Map<MpdDatasetType, ByteArray>) {
        val validFrom = getValidFrom(monthToProcess, csvMap)

        tablesProcessor.processTables(
            csvMap = csvMap,
            validFrom = validFrom,
        )

        processedDatasetRepository.save(
            ProcessedDataset(
                datasetType = DATASET_TYPE,
                year = monthToProcess.year,
                month = monthToProcess.monthValue
            )
        )

        referenceDataProvider.clearCache()
    }

    private fun getValidFrom(monthToProcess: YearMonth, csvMap: Map<MpdDatasetType, ByteArray>): LocalDate {
        return if (monthToProcess < VALIDITY_REQUIRED_SINCE) {
            monthToProcess.atDay(1)
        } else {
            val validityCsv = csvMap[MpdDatasetType.MPD_VALIDITY]
                ?: throw IllegalStateException("Validity CSV (dlp_platnost.csv) is missing for $monthToProcess")

            val validity = validityReader.getValidityFromCsv(validityCsv)
                ?: throw IllegalStateException("Unable to parse validFrom from dlp_platnost.csv for $monthToProcess")

            validity.validFrom
        }
    }
}
