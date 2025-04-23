package cz.machovec.lekovyportal.importer.mpd

import cz.machovec.lekovyportal.MpdValidityReader
import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import cz.machovec.lekovyportal.domain.entity.ProcessedDataset
import cz.machovec.lekovyportal.domain.entity.mpd.MpdCountry
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDispenseType
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCountryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDispenseTypeRepository
import cz.machovec.lekovyportal.importer.SoftDeleteWithHistory
import cz.machovec.lekovyportal.importer.common.RemoteFileDownloader
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdCountryRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdDispenseTypeRowMapper
import cz.machovec.lekovyportal.importer.columns.mpd.MpdCountryColumn
import cz.machovec.lekovyportal.importer.columns.mpd.MpdDispenseTypeColumn
import cz.machovec.lekovyportal.importer.common.CsvImporter
import cz.machovec.lekovyportal.messaging.DatasetToProcessMessage
import cz.machovec.lekovyportal.processor.DatasetProcessor
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.time.YearMonth

@Service
class MpdBundleJob(
    private val processedDatasetRepository: ProcessedDatasetRepository,
    private val countryRepo: MpdCountryRepository,
    private val dispenseTypeRepo: MpdDispenseTypeRepository,
    private val csvExtractor: MpdCsvExtractor,
    private val importer: CsvImporter,
    private val remoteFileDownloader: RemoteFileDownloader,
    private val validityReader: MpdValidityReader
) : DatasetProcessor {

    private val logger = KotlinLogging.logger {}

    companion object {
        private val DATASET_TYPE = DatasetType.MEDICINAL_PRODUCT_DATABASE
        private val FIRST_AVAILABLE_PERIOD = YearMonth.of(2021, 1)
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
        val zipBytes = remoteFileDownloader.downloadFile(URI(msg.fileUrl))
        if (zipBytes == null) {
            logger.info { "Failed to download ZIP for ${msg.fileUrl}" }
            return
        }

        // Step 3 – Extract and group CSV files by month (handles both annual and monthly ZIPs)
        val csvFilesByMonth = csvExtractor.extractMonthlyCsvFilesFromZip(zipBytes, msg)

        // Step 4 - Process each csv file
        for ((month, csvMap) in csvFilesByMonth) {
            val period = YearMonth.of(msg.year, month)
            if (canProcessMonth(period)) {
                processMonth(period, csvMap)
            }
        }
    }

    private fun processMonth(monthToProcess: YearMonth, csvMap: Map<MpdCsvFile, ByteArray>) {
        val validFrom = if (monthToProcess < VALIDITY_REQUIRED_SINCE) {
            monthToProcess.atDay(1)
        } else {
            val validityCsv = csvMap[MpdCsvFile.MPD_VALIDITY]
            if (validityCsv == null) {
                logger.warn { "Validity CSV file (dlp_platnost.csv) is missing for $monthToProcess – skipping processing." }
                return
            }

            val validity = validityReader.getValidityFromCsv(validityCsv) ?: return
            validity.validFrom
        }

        MpdCsvTableRunner(csvMap).run(
            listOf(
                MpdCsvTableRunner.TableStep(MpdCsvFile.MPD_COUNTRY) { bytes ->
                    val rows = importer.import(
                        bytes,
                        MpdCountryColumn.entries.map { it.toSpec() },
                        MpdCountryRowMapper(validFrom)
                    )
                    SoftDeleteWithHistory<MpdCountry>().apply(rows, countryRepo)
                },
                MpdCsvTableRunner.TableStep(MpdCsvFile.MPD_DISPENSE_TYPE) { bytes ->
                    val rows = importer.import(
                        bytes,
                        MpdDispenseTypeColumn.entries.map { it.toSpec() },
                        MpdDispenseTypeRowMapper(validFrom)
                    )
                    SoftDeleteWithHistory<MpdDispenseType>().apply(rows, dispenseTypeRepo)
                }
            )
        )

        processedDatasetRepository.save(
            ProcessedDataset(
                datasetType = DATASET_TYPE,
                year = monthToProcess.year,
                month = monthToProcess.monthValue
            )
        )
    }

    private fun canProcessMonth(monthToProcess: YearMonth): Boolean {
        // Case 1 – Already processed? Skip
        if (processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(
                DATASET_TYPE, monthToProcess.year, monthToProcess.monthValue)
        ) return false

        // Case 2 – First historical dataset – always process
        if (monthToProcess == FIRST_AVAILABLE_PERIOD) return true

        // Case 3 – Only process if the previous month is already in the database
        val previousMonth = monthToProcess.minusMonths(1)
        return processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(
            DATASET_TYPE, previousMonth.year, previousMonth.monthValue
        )
    }
}