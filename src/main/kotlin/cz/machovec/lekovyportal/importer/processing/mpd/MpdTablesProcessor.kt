package cz.machovec.lekovyportal.importer.processing.mpd

import MpdRegistrationExceptionRowMapper
import cz.machovec.lekovyportal.domain.entity.mpd.BaseMpdEntity
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.repository.mpd.MpdActiveSubstanceRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAddictionCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAdministrationRouteRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAtcGroupRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCancelledRegistrationRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCompositionFlagRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCountryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDispenseTypeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDopingCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDosageFormRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdGovernmentRegulationCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdIndicationGroupRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMeasurementUnitRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMedicinalProductRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMedicinalProductSubstanceRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdOrganisationRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdPackageTypeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRegistrationExceptionRepository
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
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdCancelledRegistrationColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdCancelledRegistrationRowMapper
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
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdMedicinalProductColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdMedicinalProductRowMapper
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdMedicinalProductSubstanceColumn
import cz.machovec.lekovyportal.importer.mapper.mpd.MpdMedicinalProductSubstanceRowMapper
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
    private val medicinalProductRepo: MpdMedicinalProductRepository,
    private val registrationExceptionRepo: MpdRegistrationExceptionRepository,
    private val cancelledRegistrationRepo: MpdCancelledRegistrationRepository,
    private val medicinalProductSubstanceRepo: MpdMedicinalProductSubstanceRepository,
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
            val records = deduplicateByUniqueKey(countryResult.successes, MpdDatasetType.MPD_COUNTRY.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_COUNTRY, records, countryRepo)
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
            val records = deduplicateByUniqueKey(addictionResult.successes, MpdDatasetType.MPD_ADDICTION_CATEGORY.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_ADDICTION_CATEGORY, records, addictionCategoryRepo)
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
            val records = deduplicateByUniqueKey(dopingResult.successes, MpdDatasetType.MPD_DOPING_CATEGORY.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_DOPING_CATEGORY, records, dopingCategoryRepo)
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
            val records = deduplicateByUniqueKey(govRegResult.successes, MpdDatasetType.MPD_GOVERNMENT_REGULATION_CATEGORY.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_GOVERNMENT_REGULATION_CATEGORY, records, governmentRegulationCategoryRepo)
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
            val records = deduplicateByUniqueKey(sourceResult.successes, MpdDatasetType.MPD_SOURCE.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_SOURCE, records, sourceRepo)
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
            val records = deduplicateByUniqueKey(compositionFlagResult.successes, MpdDatasetType.MPD_COMPOSITION_FLAG.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_COMPOSITION_FLAG, records, compositionFlagRepo)
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
            val records = deduplicateByUniqueKey(dispenseResult.successes, MpdDatasetType.MPD_DISPENSE_TYPE.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_DISPENSE_TYPE, records, dispenseRepo)
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
            val records = deduplicateByUniqueKey(measurementUnitResult.successes, MpdDatasetType.MPD_MEASUREMENT_UNIT.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_MEASUREMENT_UNIT, records, measurementUnitRepo)
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
            val records = deduplicateByUniqueKey(registrationProcessResult.successes, MpdDatasetType.MPD_REGISTRATION_PROCESS.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_REGISTRATION_PROCESS, records, registrationProcessRepo)
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
            val records = deduplicateByUniqueKey(registrationStatusResult.successes, MpdDatasetType.MPD_REGISTRATION_STATUS.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_REGISTRATION_STATUS, records, registrationStatusRepo)
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
            val records = deduplicateByUniqueKey(indicationGroupResult.successes, MpdDatasetType.MPD_INDICATION_GROUP.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_INDICATION_GROUP, records, indicationGroupRepo)
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
            val records = deduplicateByUniqueKey(atcGroupResult.successes, MpdDatasetType.MPD_ATC_GROUP.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_ATC_GROUP, records, atcGroupRepo)
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
            val records = deduplicateByUniqueKey(packageTypeResult.successes, MpdDatasetType.MPD_PACKAGE_TYPE.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_PACKAGE_TYPE, records, packageTypeRepo)
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
            val records = deduplicateByUniqueKey(adminRouteResult.successes, MpdDatasetType.MPD_ADMINISTRATION_ROUTE.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_ADMINISTRATION_ROUTE, records, administrationRouteRepo)
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
            val records = deduplicateByUniqueKey(dosageFormResult.successes, MpdDatasetType.MPD_DOSAGE_FORM.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_DOSAGE_FORM, records, dosageFormRepo)
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
            val records = deduplicateByUniqueKey(organisationResult.successes, MpdDatasetType.MPD_ORGANISATION.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_ORGANISATION, records, organisationRepo)
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
            val records = deduplicateByUniqueKey(activeSubstanceResult.successes, MpdDatasetType.MPD_ACTIVE_SUBSTANCE.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_ACTIVE_SUBSTANCE, records, activeSubstanceRepo)
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
            val records = deduplicateByUniqueKey(substanceResult.successes, MpdDatasetType.MPD_SUBSTANCE.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_SUBSTANCE, records, substanceRepo)
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
            val records = deduplicateByUniqueKey(synonymResult.successes, MpdDatasetType.MPD_SUBSTANCE_SYNONYM.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_SUBSTANCE_SYNONYM, records, substanceSynonymRepo)
        }

        /* ------------ MPD_MEDICINAL_PRODUCT ------------ */
        val medicinalProductCsv = csvMap[MpdDatasetType.MPD_MEDICINAL_PRODUCT]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_MEDICINAL_PRODUCT)
        val medicinalProductResult = importer.import(
            medicinalProductCsv,
            MpdMedicinalProductColumn.entries.map { it.toSpec() },
            MpdMedicinalProductRowMapper(validFrom, refProvider)
        )
        logImportSummary(MpdDatasetType.MPD_MEDICINAL_PRODUCT, medicinalProductResult)
        if (medicinalProductResult.successes.isNotEmpty()) {
            val records = deduplicateByUniqueKey(medicinalProductResult.successes, MpdDatasetType.MPD_MEDICINAL_PRODUCT.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_MEDICINAL_PRODUCT, records, medicinalProductRepo)
        }

        /* ------------ MPD_REGISTRATION_EXCEPTION ------------ */
        val registrationExceptionCsv = csvMap[MpdDatasetType.MPD_REGISTRATION_EXCEPTION]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_REGISTRATION_EXCEPTION)
        val registrationExceptionResult = importer.import(
            registrationExceptionCsv,
            MpdRegistrationExceptionColumn.entries.map { it.toSpec() },
            MpdRegistrationExceptionRowMapper(validFrom, refProvider)
        )
        logImportSummary(MpdDatasetType.MPD_REGISTRATION_EXCEPTION, registrationExceptionResult)
        if (registrationExceptionResult.successes.isNotEmpty()) {
            val records = deduplicateByUniqueKey(registrationExceptionResult.successes, MpdDatasetType.MPD_REGISTRATION_EXCEPTION.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_REGISTRATION_EXCEPTION, records, registrationExceptionRepo)
        }

        /* ------------ MPD_CANCELLED_REGISTRATION ------------ */
        val cancelledRegistrationCsv = csvMap[MpdDatasetType.MPD_CANCELLED_REGISTRATION]
        if (cancelledRegistrationCsv != null) {
            val cancelledRegistrationResult = importer.import(
                cancelledRegistrationCsv,
                MpdCancelledRegistrationColumn.entries.map { it.toSpec() },
                MpdCancelledRegistrationRowMapper(validFrom, refProvider)
            )
            logImportSummary(MpdDatasetType.MPD_CANCELLED_REGISTRATION, cancelledRegistrationResult)
            if (cancelledRegistrationResult.successes.isNotEmpty()) {
                val records = deduplicateByUniqueKey(cancelledRegistrationResult.successes, MpdDatasetType.MPD_CANCELLED_REGISTRATION.name)
                synchronizer.sync(validFrom, MpdDatasetType.MPD_CANCELLED_REGISTRATION, records, cancelledRegistrationRepo)
            }
        } else {
            logger.info { "Optional dataset ${MpdDatasetType.MPD_CANCELLED_REGISTRATION} not found. Skipping import." }
        }

        /* ------------ MPD_MEDICINAL_PRODUCT_SUBSTANCE ------------ */
        val medicinalProductSubstanceCsv = csvMap[MpdDatasetType.MPD_MEDICINAL_PRODUCT_SUBSTANCE]
            ?: throw MissingCsvFileException(MpdDatasetType.MPD_MEDICINAL_PRODUCT_SUBSTANCE)
        val medicinalProductSubstanceResult = importer.import(
            medicinalProductSubstanceCsv,
            MpdMedicinalProductSubstanceColumn.entries.map { it.toSpec() },
            MpdMedicinalProductSubstanceRowMapper(validFrom, refProvider)
        )
        logImportSummary(MpdDatasetType.MPD_MEDICINAL_PRODUCT_SUBSTANCE, medicinalProductSubstanceResult)
        if (medicinalProductSubstanceResult.successes.isNotEmpty()) {
            val records = deduplicateByUniqueKey(medicinalProductSubstanceResult.successes, MpdDatasetType.MPD_MEDICINAL_PRODUCT_SUBSTANCE.name)
            synchronizer.sync(validFrom, MpdDatasetType.MPD_MEDICINAL_PRODUCT_SUBSTANCE, records, medicinalProductSubstanceRepo)
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

    private fun <T : BaseMpdEntity<T>> deduplicateByUniqueKey(
        records: List<T>,
        datasetTypeName: String
    ): List<T> {
        return records
            .groupBy { it.getUniqueKey() }
            .mapValues { (_, duplicates) ->
                if (duplicates.size > 1) {
                    logger.warn { "$datasetTypeName - Duplicate unique key '${duplicates.first().getUniqueKey()}' found ${duplicates.size}x. Keeping the first, ignoring others." }
                }
                duplicates.first()
            }
            .values
            .toList()
    }
}

/**
 * Exception thrown when a required CSV file is missing in the bundle.
 */
class MissingCsvFileException(datasetType: MpdDatasetType) :
    RuntimeException("Missing required CSV file for dataset: ${datasetType.fileName}")
