package cz.machovec.lekovyportal.importer.processing.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.repository.mpd.MpdActiveSubstanceRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAddictionCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAdministrationRouteRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAtcGroupRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCompositionFlagRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCountryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDispenseTypeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDopingCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDosageFormRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdGovernmentRegulationCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdIndicationGroupRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMeasurementUnitRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdOrganisationRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdPackageTypeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRegistrationProcessRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRegistrationStatusRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdSourceRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdSubstanceRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdSubstanceSynonymRepository
import cz.machovec.lekovyportal.importer.common.CsvImporter
import cz.machovec.lekovyportal.importer.mapper.DataImportResult
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdActiveSubstanceColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdActiveSubstanceRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdAddictionCategoryColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdAddictionCategoryRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdAdministrationRouteColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdAdministrationRouteRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdAtcGroupColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdAtcGroupRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdCompositionFlagColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdCompositionFlagRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdCountryColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdCountryRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdDispenseTypeColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdDispenseTypeRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdDopingCategoryColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdDopingCategoryRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdDosageFormColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdDosageFormRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdGovernmentRegulationCategoryColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdGovernmentRegulationCategoryRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdIndicationGroupColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdIndicationGroupRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdMeasurementUnitColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdMeasurementUnitRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdOrganisationColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdOrganisationRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdPackageTypeColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdPackageTypeRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdRegistrationProcessColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdRegistrationProcessRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdRegistrationStatusColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdRegistrationStatusRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdSourceColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdSourceRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdSubstanceColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdSubstanceRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdSubstanceSynonymColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdSubstanceSynonymRowMapper
import cz.machovec.lekovyportal.importer.mapper.toSpec
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class MpdTablesProcessor(
    private val importer: CsvImporter,
    private val synchronizer: MpdEntitySynchronizer,
    private val countryRepo: MpdCountryRepository,
    private val addictionCategoryRepo: MpdAddictionCategoryRepository,
    private val dopingCategoryRepo: MpdDopingCategoryRepository,
    private val governmentRegulationCategoryRepo: MpdGovernmentRegulationCategoryRepository,
    private val sourceRepo: MpdSourceRepository,
    private val compositionFlagRepo: MpdCompositionFlagRepository,
    private val dispenseRepo: MpdDispenseTypeRepository,
    private val measurementUnitRepo: MpdMeasurementUnitRepository,
    private val registrationProcessRepo: MpdRegistrationProcessRepository,
    private val registrationStatusRepo: MpdRegistrationStatusRepository,
    private val indicationGroupRepo: MpdIndicationGroupRepository,
    private val atcGroupRepo: MpdAtcGroupRepository,
    private val packageTypeRepo: MpdPackageTypeRepository,
    private val administrationRouteRepo: MpdAdministrationRouteRepository,
    private val dosageFormRepo: MpdDosageFormRepository,
    private val organisationRepo: MpdOrganisationRepository,
    private val activeSubstanceRepo: MpdActiveSubstanceRepository,
    private val substanceRepo: MpdSubstanceRepository,
    private val substanceSynonymRepo: MpdSubstanceSynonymRepository,
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
            synchronizer.sync(validFrom, MpdDatasetType.MPD_COUNTRY, countryResult.successes, countryRepo)
        }

        /* ------------ MPD_ADDICTION_CATEGORY ------------ */
        val addictionCsv = csvMap[MpdDatasetType.MPD_ADDICTION_CATEGORY]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_ADDICTION_CATEGORY)
        val addictionResult = importer.import(
            addictionCsv,
            MpdAddictionCategoryColumn.entries.map { it.toSpec() },
            MpdAddictionCategoryRowMapper(validFrom)
        )
        logImportSummary(MpdDatasetType.MPD_ADDICTION_CATEGORY, addictionResult)
        if (addictionResult.successes.isNotEmpty()) {
            synchronizer.sync(validFrom, MpdDatasetType.MPD_ADDICTION_CATEGORY, addictionResult.successes, addictionCategoryRepo)
        }

        /* ------------ MPD_DOPING_CATEGORY ------------ */
        val dopingCsv = csvMap[MpdDatasetType.MPD_DOPING_CATEGORY]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_DOPING_CATEGORY)
        val dopingResult = importer.import(
            dopingCsv,
            MpdDopingCategoryColumn.entries.map { it.toSpec() },
            MpdDopingCategoryRowMapper(validFrom)
        )
        logImportSummary(MpdDatasetType.MPD_DOPING_CATEGORY, dopingResult)
        if (dopingResult.successes.isNotEmpty()) {
            synchronizer.sync(validFrom, MpdDatasetType.MPD_DOPING_CATEGORY, dopingResult.successes, dopingCategoryRepo)
        }

        /* ------------ MPD_GOVERNMENT_REGULATION_CATEGORY ------------ */
        val govRegCsv = csvMap[MpdDatasetType.MPD_GOVERNMENT_REGULATION_CATEGORY]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_GOVERNMENT_REGULATION_CATEGORY)
        val govRegResult = importer.import(
            govRegCsv,
            MpdGovernmentRegulationCategoryColumn.entries.map { it.toSpec() },
            MpdGovernmentRegulationCategoryRowMapper(validFrom)
        )
        logImportSummary(MpdDatasetType.MPD_GOVERNMENT_REGULATION_CATEGORY, govRegResult)
        if (govRegResult.successes.isNotEmpty()) {
            synchronizer.sync(validFrom, MpdDatasetType.MPD_GOVERNMENT_REGULATION_CATEGORY, govRegResult.successes, governmentRegulationCategoryRepo)
        }

        /* ------------ MPD_SOURCE ------------ */
        val sourceCsv = csvMap[MpdDatasetType.MPD_SOURCE]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_SOURCE)
        val sourceResult = importer.import(
            sourceCsv,
            MpdSourceColumn.entries.map { it.toSpec() },
            MpdSourceRowMapper(validFrom)
        )
        logImportSummary(MpdDatasetType.MPD_SOURCE, sourceResult)
        if (sourceResult.successes.isNotEmpty()) {
            synchronizer.sync(validFrom, MpdDatasetType.MPD_SOURCE, sourceResult.successes, sourceRepo)
        }

        /* ------------ MPD_COMPOSITION_FLAG ------------ */
        val compositionFlagCsv = csvMap[MpdDatasetType.MPD_COMPOSITION_FLAG]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_COMPOSITION_FLAG)
        val compositionFlagResult = importer.import(
            compositionFlagCsv,
            MpdCompositionFlagColumn.entries.map { it.toSpec() },
            MpdCompositionFlagRowMapper(validFrom)
        )
        logImportSummary(MpdDatasetType.MPD_COMPOSITION_FLAG, compositionFlagResult)
        if (compositionFlagResult.successes.isNotEmpty()) {
            synchronizer.sync(validFrom, MpdDatasetType.MPD_COMPOSITION_FLAG, compositionFlagResult.successes, compositionFlagRepo)
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
            synchronizer.sync(validFrom, MpdDatasetType.MPD_DISPENSE_TYPE, dispenseResult.successes, dispenseRepo)
        }

        /* ------------ MPD_MEASUREMENT_UNIT ------------ */
        val measurementUnitCsv = csvMap[MpdDatasetType.MPD_MEASUREMENT_UNIT]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_MEASUREMENT_UNIT)
        val measurementUnitResult = importer.import(
            measurementUnitCsv,
            MpdMeasurementUnitColumn.entries.map { it.toSpec() },
            MpdMeasurementUnitRowMapper(validFrom)
        )
        logImportSummary(MpdDatasetType.MPD_MEASUREMENT_UNIT, measurementUnitResult)
        if (measurementUnitResult.successes.isNotEmpty()) {
            synchronizer.sync(validFrom, MpdDatasetType.MPD_MEASUREMENT_UNIT, measurementUnitResult.successes, measurementUnitRepo)
        }

        /* ------------ MPD_REGISTRATION_PROCESS ------------ */
        val registrationProcessCsv = csvMap[MpdDatasetType.MPD_REGISTRATION_PROCESS]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_REGISTRATION_PROCESS)
        val registrationProcessResult = importer.import(
            registrationProcessCsv,
            MpdRegistrationProcessColumn.entries.map { it.toSpec() },
            MpdRegistrationProcessRowMapper(validFrom)
        )
        logImportSummary(MpdDatasetType.MPD_REGISTRATION_PROCESS, registrationProcessResult)
        if (registrationProcessResult.successes.isNotEmpty()) {
            synchronizer.sync(validFrom, MpdDatasetType.MPD_REGISTRATION_PROCESS, registrationProcessResult.successes, registrationProcessRepo)
        }

        /* ------------ MPD_REGISTRATION_STATUS ------------ */
        val registrationStatusCsv = csvMap[MpdDatasetType.MPD_REGISTRATION_STATUS]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_REGISTRATION_STATUS)
        val registrationStatusResult = importer.import(
            registrationStatusCsv,
            MpdRegistrationStatusColumn.entries.map { it.toSpec() },
            MpdRegistrationStatusRowMapper(validFrom)
        )
        logImportSummary(MpdDatasetType.MPD_REGISTRATION_STATUS, registrationStatusResult)
        if (registrationStatusResult.successes.isNotEmpty()) {
            synchronizer.sync(validFrom, MpdDatasetType.MPD_REGISTRATION_STATUS, registrationStatusResult.successes, registrationStatusRepo)
        }

        /* ------------ MPD_INDICATION_GROUP ------------ */
        val indicationGroupCsv = csvMap[MpdDatasetType.MPD_INDICATION_GROUP]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_INDICATION_GROUP)
        val indicationGroupResult = importer.import(
            indicationGroupCsv,
            MpdIndicationGroupColumn.entries.map { it.toSpec() },
            MpdIndicationGroupRowMapper(validFrom)
        )
        logImportSummary(MpdDatasetType.MPD_INDICATION_GROUP, indicationGroupResult)
        if (indicationGroupResult.successes.isNotEmpty()) {
            synchronizer.sync(validFrom, MpdDatasetType.MPD_INDICATION_GROUP, indicationGroupResult.successes, indicationGroupRepo)
        }

        /* ------------ MPD_ATC_GROUP ------------ */
        val atcGroupCsv = csvMap[MpdDatasetType.MPD_ATC_GROUP]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_ATC_GROUP)
        val atcGroupResult = importer.import(
            atcGroupCsv,
            MpdAtcGroupColumn.entries.map { it.toSpec() },
            MpdAtcGroupRowMapper(validFrom)
        )
        logImportSummary(MpdDatasetType.MPD_ATC_GROUP, atcGroupResult)
        if (atcGroupResult.successes.isNotEmpty()) {
            synchronizer.sync(validFrom, MpdDatasetType.MPD_ATC_GROUP, atcGroupResult.successes, atcGroupRepo)
        }

        /* ------------ MPD_PACKAGE_TYPE ------------ */
        val packageTypeCsv = csvMap[MpdDatasetType.MPD_PACKAGE_TYPE]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_PACKAGE_TYPE)
        val packageTypeResult = importer.import(
            packageTypeCsv,
            MpdPackageTypeColumn.entries.map { it.toSpec() },
            MpdPackageTypeRowMapper(validFrom)
        )
        logImportSummary(MpdDatasetType.MPD_PACKAGE_TYPE, packageTypeResult)
        if (packageTypeResult.successes.isNotEmpty()) {
            synchronizer.sync(validFrom, MpdDatasetType.MPD_PACKAGE_TYPE, packageTypeResult.successes, packageTypeRepo)
        }

        /* ------------ MPD_ADMINISTRATION_ROUTE ------------ */
        val adminRouteCsv = csvMap[MpdDatasetType.MPD_ADMINISTRATION_ROUTE]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_ADMINISTRATION_ROUTE)
        val adminRouteResult = importer.import(
            adminRouteCsv,
            MpdAdministrationRouteColumn.entries.map { it.toSpec() },
            MpdAdministrationRouteRowMapper(validFrom)
        )
        logImportSummary(MpdDatasetType.MPD_ADMINISTRATION_ROUTE, adminRouteResult)
        if (adminRouteResult.successes.isNotEmpty()) {
            synchronizer.sync(validFrom, MpdDatasetType.MPD_ADMINISTRATION_ROUTE, adminRouteResult.successes, administrationRouteRepo)
        }

        /* ------------ MPD_DOSAGE_FORM ------------ */
        val dosageFormCsv = csvMap[MpdDatasetType.MPD_DOSAGE_FORM]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_DOSAGE_FORM)
        val dosageFormResult = importer.import(
            dosageFormCsv,
            MpdDosageFormColumn.entries.map { it.toSpec() },
            MpdDosageFormRowMapper(validFrom)
        )
        logImportSummary(MpdDatasetType.MPD_DOSAGE_FORM, dosageFormResult)
        if (dosageFormResult.successes.isNotEmpty()) {
            synchronizer.sync(validFrom, MpdDatasetType.MPD_DOSAGE_FORM, dosageFormResult.successes, dosageFormRepo)
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
            synchronizer.sync(validFrom, MpdDatasetType.MPD_ORGANISATION, organisationResult.successes, organisationRepo)
        }

        /* ------------ MPD_ACTIVE_SUBSTANCE ------------ */
        val activeSubstanceCsv = csvMap[MpdDatasetType.MPD_ACTIVE_SUBSTANCE]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_ACTIVE_SUBSTANCE)
        val activeSubstanceResult = importer.import(
            activeSubstanceCsv,
            MpdActiveSubstanceColumn.entries.map { it.toSpec() },
            MpdActiveSubstanceRowMapper(validFrom, refProvider)
        )
        logImportSummary(MpdDatasetType.MPD_ACTIVE_SUBSTANCE, activeSubstanceResult)
        if (activeSubstanceResult.successes.isNotEmpty()) {
            synchronizer.sync(validFrom, MpdDatasetType.MPD_ACTIVE_SUBSTANCE, activeSubstanceResult.successes, activeSubstanceRepo)
        }

        /* ------------ MPD_SUBSTANCE ------------ */
        val substanceCsv = csvMap[MpdDatasetType.MPD_SUBSTANCE]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_SUBSTANCE)
        val substanceResult = importer.import(
            substanceCsv,
            MpdSubstanceColumn.entries.map { it.toSpec() },
            MpdSubstanceRowMapper(validFrom, refProvider)
        )
        logImportSummary(MpdDatasetType.MPD_SUBSTANCE, substanceResult)
        if (substanceResult.successes.isNotEmpty()) {
            synchronizer.sync(validFrom, MpdDatasetType.MPD_SUBSTANCE, substanceResult.successes, substanceRepo)
        }

        /* ------------ MPD_SUBSTANCE_SYNONYM ------------ */
        val synonymCsv = csvMap[MpdDatasetType.MPD_SUBSTANCE_SYNONYM]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_SUBSTANCE_SYNONYM)
        val synonymResult = importer.import(
            synonymCsv,
            MpdSubstanceSynonymColumn.entries.map { it.toSpec() },
            MpdSubstanceSynonymRowMapper(validFrom, refProvider)
        )
        logImportSummary(MpdDatasetType.MPD_SUBSTANCE_SYNONYM, synonymResult)
        if (synonymResult.successes.isNotEmpty()) {
            synchronizer.sync(validFrom, MpdDatasetType.MPD_SUBSTANCE_SYNONYM, synonymResult.successes, substanceSynonymRepo)
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
