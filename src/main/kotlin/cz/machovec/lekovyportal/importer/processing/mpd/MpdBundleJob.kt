package cz.machovec.lekovyportal.importer.processing.mpd

import cz.machovec.lekovyportal.MpdValidityReader
import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import cz.machovec.lekovyportal.domain.entity.ProcessedDataset
import cz.machovec.lekovyportal.domain.entity.mpd.MpdCountry
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDispenseType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdOrganisation
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCountryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDispenseTypeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdOrganisationRepository
import cz.machovec.lekovyportal.importer.SoftDeleteWithHistory
import cz.machovec.lekovyportal.importer.common.RemoteFileDownloader
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdCountryRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdDispenseTypeRowMapper
import cz.machovec.lekovyportal.importer.columns.mpd.MpdCountryColumn
import cz.machovec.lekovyportal.importer.columns.mpd.MpdDispenseTypeColumn
import cz.machovec.lekovyportal.importer.columns.mpd.MpdOrganisationColumn
import cz.machovec.lekovyportal.importer.common.CsvImporter
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdOrganisationRowMapper
import cz.machovec.lekovyportal.importer.processing.DatasetProcessingEvaluator
import cz.machovec.lekovyportal.messaging.DatasetToProcessMessage
import cz.machovec.lekovyportal.processor.DatasetProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider
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
    private val organisationRepository: MpdOrganisationRepository,
    private val csvExtractor: MpdCsvExtractor,
    private val importer: CsvImporter,
    private val downloader: RemoteFileDownloader,
    private val validityReader: MpdValidityReader,
    private val referenceDataProvider: MpdReferenceDataProvider,
    private val datasetProcessingEvaluator: DatasetProcessingEvaluator
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

        // Step 4 - Process each csv file
        for ((month, csvMap) in csvFilesByMonth) {
            if (datasetProcessingEvaluator.canProcessMonth(msg.datasetType, msg.year, month)) {
                val period = YearMonth.of(msg.year, month)
                processMonth(period, csvMap)
            }
        }
    }

    private fun processMonth(monthToProcess: YearMonth, csvMap: Map<MpdDatasetType, ByteArray>) {
        val validFrom = if (monthToProcess < VALIDITY_REQUIRED_SINCE) {
            monthToProcess.atDay(1)
        } else {
            val validityCsv = csvMap[MpdDatasetType.MPD_VALIDITY]
            if (validityCsv == null) {
                logger.warn { "Validity CSV file (dlp_platnost.csv) is missing for $monthToProcess – skipping processing." }
                return
            }

            val validity = validityReader.getValidityFromCsv(validityCsv) ?: return
            validity.validFrom
        }

        MpdCsvTableRunner(csvMap).run(
            listOf(
                MpdCsvTableRunner.TableStep(MpdDatasetType.MPD_COUNTRY) { bytes ->
                    val rows = importer.import(
                        bytes,
                        MpdCountryColumn.entries.map { it.toSpec() },
                        MpdCountryRowMapper(validFrom)
                    )
                    SoftDeleteWithHistory<MpdCountry>().apply(rows, countryRepo)
                },
                MpdCsvTableRunner.TableStep(MpdDatasetType.MPD_DISPENSE_TYPE) { bytes ->
                    val rows = importer.import(
                        bytes,
                        MpdDispenseTypeColumn.entries.map { it.toSpec() },
                        MpdDispenseTypeRowMapper(validFrom)
                    )
                    SoftDeleteWithHistory<MpdDispenseType>().apply(rows, dispenseTypeRepo)
                },
                MpdCsvTableRunner.TableStep(MpdDatasetType.MPD_ORGANISATION) { bytes ->
                    val rows = importer.import(
                        bytes,
                        MpdOrganisationColumn.entries.map { it.toSpec() },
                        MpdOrganisationRowMapper(validFrom, referenceDataProvider)
                    )
                    SoftDeleteWithHistory<MpdOrganisation>().apply(rows, organisationRepository)
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
}