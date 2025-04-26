package cz.machovec.lekovyportal.importer.processing.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCountryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDispenseTypeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdOrganisationRepository
import cz.machovec.lekovyportal.importer.common.CsvImporter
import cz.machovec.lekovyportal.importer.mapper.DataImportResult
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdCountryColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdCountryRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdDispenseTypeColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdDispenseTypeRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdOrganisationColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdOrganisationRowMapper
import cz.machovec.lekovyportal.importer.mapper.toSpec
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class MpdTablesProcessor(
    private val importer: CsvImporter,
    private val synchronizer: MpdEntitySynchronizer,
    private val countryRepo: MpdCountryRepository,
    private val dispenseRepo: MpdDispenseTypeRepository,
    private val organisationRepo: MpdOrganisationRepository,
    private val refProvider: MpdReferenceDataProvider
) {

    private val logger = KotlinLogging.logger {}

    /**
     * Processes all expected MPD tables from the given CSV map.
     * Throws [MissingCsvFileException] if any required file is missing.
     */
    fun processTables(
        csvMap: Map<MpdDatasetType, ByteArray>,
        validFrom: LocalDate,
    ) {
        /* ------------ MPD_COUNTRY ------------ */
        val countryCsv = csvMap[MpdDatasetType.MPD_COUNTRY]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_COUNTRY)
        val countryResult = importer.import(
            countryCsv,
            MpdCountryColumn.entries.map { it.toSpec() },
            MpdCountryRowMapper(validFrom)
        )
        logImportSummary(MpdDatasetType.MPD_COUNTRY, countryResult)
        if (countryResult.successes.isNotEmpty()) {
            synchronizer.sync(
                validFrom = validFrom,
                dataset = MpdDatasetType.MPD_COUNTRY,
                records = countryResult.successes,
                repo = countryRepo
            )
        }

        /* ------------ MPD_DISPENSE_TYPE ------------ */
        val dispenseCsv = csvMap[MpdDatasetType.MPD_DISPENSE_TYPE]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_DISPENSE_TYPE)
        val dispenseResult = importer.import(
            dispenseCsv,
            MpdDispenseTypeColumn.entries.map { it.toSpec() },
            MpdDispenseTypeRowMapper(validFrom)
        )
        logImportSummary(MpdDatasetType.MPD_DISPENSE_TYPE, dispenseResult)
        if (dispenseResult.successes.isNotEmpty()) {
            synchronizer.sync(
                validFrom = validFrom,
                dataset = MpdDatasetType.MPD_DISPENSE_TYPE,
                records = dispenseResult.successes,
                repo = dispenseRepo
            )
        }

        /* ------------ MPD_ORGANISATION ------------ */
        val organisationCsv = csvMap[MpdDatasetType.MPD_ORGANISATION]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_ORGANISATION)
        val organisationResult = importer.import(
            organisationCsv,
            MpdOrganisationColumn.entries.map { it.toSpec() },
            MpdOrganisationRowMapper(validFrom, refProvider)
        )
        logImportSummary(MpdDatasetType.MPD_ORGANISATION, organisationResult)
        if (organisationResult.successes.isNotEmpty()) {
            synchronizer.sync(
                validFrom = validFrom,
                dataset = MpdDatasetType.MPD_ORGANISATION,
                records = organisationResult.successes,
                repo = organisationRepo
            )
        }
    }

    private fun <T> logImportSummary(datasetType: MpdDatasetType, result: DataImportResult<T>) {
        if (result.failures.isEmpty()) {
            logger.info { "Import of $datasetType completed successfully (${result.successes.size}/${result.totalRows} rows)." }
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
        Import of $datasetType completed with errors:
          - Success: ${result.successes.size}/${result.totalRows}
          - Failures by reason: $reasonSummary
          - Failures by reason and column:
            $detailedSummary
        """.trimIndent()
        }
    }
}

/**
 * Exception thrown when a required CSV file is missing in the bundle.
 */
class MissingCsvFileException(datasetType: MpdDatasetType) :
    RuntimeException("Missing required CSV file for dataset: ${datasetType.fileName}")
