package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProduct
import cz.machovec.lekovyportal.domain.entity.mpd.MpdOrganisation
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMedicinalProductRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

@Service
class MpdMedicinalProductProcessor(
    medicinalProductRepository: MpdMedicinalProductRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository,
    private val referenceDataProvider: MpdReferenceDataProvider
) : BaseMpdProcessor<MpdMedicinalProduct>(
    medicinalProductRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {
    companion object {
        private const val COLUMN_SUKL_CODE = "SUKL_KOD"
        private const val COLUMN_REPORTING_OBLIGATION = "HLASENI"
        private const val COLUMN_NAME = "NAZEV"
        private const val COLUMN_STRENGTH = "SILA"
        private const val COLUMN_DOSAGE_FORM = "FORMA"
        private const val COLUMN_PACKAGING = "BALENI"
        private const val COLUMN_ADMIN_ROUTE = "CESTA"
        private const val COLUMN_SUPPLEMENTARY_INFO = "DOPLNEK"
        private const val COLUMN_PACKAGE_TYPE = "DRUH_BALENI"
        private const val COLUMN_MAH_CODE = "DRZITEL_KOD"
        private const val COLUMN_MAH_COUNTRY = "DRZITEL_ZEME"
        private const val COLUMN_CURR_MAH_CODE = "AKT_DRZITEL_KOD"
        private const val COLUMN_CURR_MAH_COUNTRY = "AKT_DRZITEL_ZEME"
        private const val COLUMN_REGISTRATION_STATUS = "STAV"
        private const val COLUMN_REGISTRATION_VALID_TO = "PLATNOST_DO"
        private const val COLUMN_REGISTRATION_UNLIMITED = "NEOMEZENE"
        private const val COLUMN_MARKET_SUPPLY_END = "KONEC_DODAVEK"
        private const val COLUMN_INDICATION_GROUP = "INDIKACE"
        private const val COLUMN_ATC_GROUP = "ATC"
        private const val COLUMN_REGISTRATION_NUMBER = "REG_CISLO"
        private const val COLUMN_PARALLEL_IMPORT_ID = "PAR_ID"
        private const val COLUMN_PARALLEL_IMPORT_SUPPLIER_CODE = "PAR_DOD_KOD"
        private const val COLUMN_PARALLEL_IMPORT_SUPPLIER_COUNTRY = "PAR_DOD_ZEME"
        private const val COLUMN_REGISTRATION_PROCESS = "REG_PROC"
        private const val COLUMN_DAILY_DOSE_AMOUNT = "DDD_MNOZSTVI"
        private const val COLUMN_DAILY_DOSE_UNIT = "DDD_JEDNOTKA"
        private const val COLUMN_DAILY_DOSE_PACKAGING = "DDD_BALENI"
        private const val COLUMN_WHO_SOURCE = "WHO"
        private const val COLUMN_SUBSTANCE_LIST = "LATKY"
        private const val COLUMN_DISPENSE_TYPE = "VYDEJ"
        private const val COLUMN_ADDICTION_CATEGORY = "ZAVISLOST"
        private const val COLUMN_DOPING_CATEGORY = "DOPING"
        private const val COLUMN_GOV_REG_CATEGORY = "VLADNI_REGULACE"
        private const val COLUMN_DELIVERIES_FLAG = "DODAVKY"
        private const val COLUMN_EAN = "EAN"
        private const val COLUMN_BRAILLE = "BRAILLOVO_PISMO"
        private const val COLUMN_EXPIRY_PERIOD_DURATION = "EXP_DOBA"
        private const val COLUMN_EXPIRY_PERIOD_UNIT = "EXP_JEDNOTKA"
        private const val COLUMN_REGISTERED_NAME = "REG_NAZEV"
        private const val COLUMN_MRP_NUMBER = "MRP_CISLO"
        private const val COLUMN_REGISTRATION_LEGAL_BASIS = "PRAVNI_ZAKLAD"
        private const val COLUMN_SAFETY_FEATURE = "BEZPECNOSTNI_PRVEK"
        private const val COLUMN_PRESCRIPTION_RESTRICTION = "OMEZENI_PREDPISU"
        private const val COLUMN_MEDICINAL_PRODUCT_TYPE = "TYP"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_MEDICINAL_PRODUCT

    override fun getExpectedColumns(): List<String> = listOf(
        COLUMN_SUKL_CODE,
        COLUMN_REPORTING_OBLIGATION,
        COLUMN_NAME,
        COLUMN_STRENGTH,
        COLUMN_DOSAGE_FORM,
        COLUMN_PACKAGING,
        COLUMN_ADMIN_ROUTE,
        COLUMN_SUPPLEMENTARY_INFO,
        COLUMN_PACKAGE_TYPE,
        COLUMN_MAH_CODE,
        COLUMN_MAH_COUNTRY,
        COLUMN_CURR_MAH_CODE,
        COLUMN_CURR_MAH_COUNTRY,
        COLUMN_REGISTRATION_STATUS,
        COLUMN_REGISTRATION_VALID_TO,
        COLUMN_REGISTRATION_UNLIMITED,
        COLUMN_MARKET_SUPPLY_END,
        COLUMN_INDICATION_GROUP,
        COLUMN_ATC_GROUP,
        COLUMN_REGISTRATION_NUMBER,
        COLUMN_PARALLEL_IMPORT_ID,
        COLUMN_PARALLEL_IMPORT_SUPPLIER_CODE,
        COLUMN_PARALLEL_IMPORT_SUPPLIER_COUNTRY,
        COLUMN_REGISTRATION_PROCESS,
        COLUMN_DAILY_DOSE_AMOUNT,
        COLUMN_DAILY_DOSE_UNIT,
        COLUMN_DAILY_DOSE_PACKAGING,
        COLUMN_WHO_SOURCE,
        COLUMN_SUBSTANCE_LIST,
        COLUMN_DISPENSE_TYPE,
        COLUMN_ADDICTION_CATEGORY,
        COLUMN_DOPING_CATEGORY,
        COLUMN_GOV_REG_CATEGORY,
        COLUMN_DELIVERIES_FLAG,
        COLUMN_EAN,
        COLUMN_BRAILLE,
        COLUMN_EXPIRY_PERIOD_DURATION,
        COLUMN_EXPIRY_PERIOD_UNIT,
        COLUMN_REGISTERED_NAME,
        COLUMN_MRP_NUMBER,
        COLUMN_REGISTRATION_LEGAL_BASIS,
        COLUMN_SAFETY_FEATURE,
        COLUMN_PRESCRIPTION_RESTRICTION,
        COLUMN_MEDICINAL_PRODUCT_TYPE
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdMedicinalProduct? {
        // TODO změnit nullable atributů
        try {
            val suklCode = row[headerIndex.getValue(COLUMN_SUKL_CODE)].trim()
            val reportingObligation = row[headerIndex.getValue(COLUMN_REPORTING_OBLIGATION)].trim() == "X"
            val name = row[headerIndex.getValue(COLUMN_NAME)].trim()
            val strength = row[headerIndex.getValue(COLUMN_STRENGTH)].trim().ifBlank { null }
            val dosageForm = headerIndex[COLUMN_DOSAGE_FORM]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getDosageForms()[it] }
            val packaging = row[headerIndex.getValue(COLUMN_PACKAGING)].trim().ifBlank { null }
            val administrationRoute = headerIndex[COLUMN_ADMIN_ROUTE]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getAdministrationRoutes()[it] }
            val supplementaryInformation = row[headerIndex.getValue(COLUMN_SUPPLEMENTARY_INFO)].trim().ifBlank { null }
            val packageType = headerIndex[COLUMN_PACKAGE_TYPE]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getPackageTypes()[it] }

            val marketingAuthorizationHolder = findOrganisation(
                row[headerIndex.getValue(COLUMN_MAH_CODE)],
                row[headerIndex.getValue(COLUMN_MAH_COUNTRY)]
            )
            val currentMarketingAuthorizationHolder = findOrganisation(
                row[headerIndex.getValue(COLUMN_CURR_MAH_CODE)],
                row[headerIndex.getValue(COLUMN_CURR_MAH_COUNTRY)]
            )

            val registrationStatus = headerIndex[COLUMN_REGISTRATION_STATUS]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getRegistrationStatuses()[it] }
            val registrationValidTo = parseDate(row[headerIndex.getValue(COLUMN_REGISTRATION_VALID_TO)].trim())
            val registrationUnlimited = row[headerIndex.getValue(COLUMN_REGISTRATION_UNLIMITED)].trim() == "X"
            val marketSupplyEndDate = parseDate(row[headerIndex.getValue(COLUMN_MARKET_SUPPLY_END)].trim())

            val indicationGroup = headerIndex[COLUMN_INDICATION_GROUP]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getIndicationGroups()[it] }
            val atcGroup = headerIndex[COLUMN_ATC_GROUP]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getAtcGroups()[it] }

            val registrationNumber = row[headerIndex.getValue(COLUMN_REGISTRATION_NUMBER)].trim().ifBlank { null }
            val parallelImportId = row[headerIndex.getValue(COLUMN_PARALLEL_IMPORT_ID)].trim().ifBlank { null }
            val parallelImportSupplier = findOrganisation(
                row[headerIndex.getValue(COLUMN_PARALLEL_IMPORT_SUPPLIER_CODE)],
                row[headerIndex.getValue(COLUMN_PARALLEL_IMPORT_SUPPLIER_COUNTRY)]
            )

            val registrationProcess = headerIndex[COLUMN_REGISTRATION_PROCESS]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getRegistrationProcesses()[it] }
            val dailyDoseAmount = row[headerIndex.getValue(COLUMN_DAILY_DOSE_AMOUNT)].trim().toBigDecimalOrNull()
            val dailyDoseUnit = headerIndex[COLUMN_DAILY_DOSE_UNIT]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getMeasurementUnits()[it] }
            val dailyDosePackaging = row[headerIndex.getValue(COLUMN_DAILY_DOSE_PACKAGING)].trim().toBigDecimalOrNull()
            val whoSource = row[headerIndex.getValue(COLUMN_WHO_SOURCE)].trim().ifBlank { null }
            val substanceList = row[headerIndex.getValue(COLUMN_SUBSTANCE_LIST)].trim().ifBlank { null }

            val dispenseType = headerIndex[COLUMN_DISPENSE_TYPE]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getDispenseTypes()[it] }
            val addictionCategory = headerIndex[COLUMN_ADDICTION_CATEGORY]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getAddictionCategories()[it] }
            val dopingCategory = headerIndex[COLUMN_DOPING_CATEGORY]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getDopingCategories()[it] }
            val governmentRegulationCategory = headerIndex[COLUMN_GOV_REG_CATEGORY]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getGovRegulationCategories()[it] }

            val deliveriesFlag = row[headerIndex.getValue(COLUMN_DELIVERIES_FLAG)].trim() == "X"
            val ean = row[headerIndex.getValue(COLUMN_EAN)].trim().ifBlank { null }
            val braille = row[headerIndex.getValue(COLUMN_BRAILLE)].trim().ifBlank { null }
            val expiryPeriodDuration = row[headerIndex.getValue(COLUMN_EXPIRY_PERIOD_DURATION)].trim().ifBlank { null }
            val expiryPeriodUnit = row[headerIndex.getValue(COLUMN_EXPIRY_PERIOD_UNIT)].trim().ifBlank { null }
            val registeredName = row[headerIndex.getValue(COLUMN_REGISTERED_NAME)].trim().ifBlank { null }

            val mrpNumber = headerIndex[COLUMN_MRP_NUMBER]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }
            val registrationLegalBasis = headerIndex[COLUMN_REGISTRATION_LEGAL_BASIS]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }
            val safetyFeature = headerIndex[COLUMN_SAFETY_FEATURE]?.let { row.getOrNull(it)?.trim() == "A" } ?: false
            val prescriptionRestriction = headerIndex[COLUMN_PRESCRIPTION_RESTRICTION]?.let { row.getOrNull(it)?.trim() == "A" } ?: false
            val medicinalProductType = headerIndex[COLUMN_MEDICINAL_PRODUCT_TYPE]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }

            return MpdMedicinalProduct(
                suklCode = suklCode,
                reportingObligation = reportingObligation,
                name = name,
                strength = strength,
                dosageForm = dosageForm,
                packaging = packaging,
                administrationRoute = administrationRoute,
                supplementaryInformation = supplementaryInformation,
                packageType = packageType,
                marketingAuthorizationHolder = marketingAuthorizationHolder,
                currentMarketingAuthorizationHolder = currentMarketingAuthorizationHolder,
                registrationStatus = registrationStatus,
                registrationValidTo = registrationValidTo,
                registrationUnlimited = registrationUnlimited,
                marketSupplyEndDate = marketSupplyEndDate,
                indicationGroup = indicationGroup,
                atcGroup = atcGroup,
                registrationNumber = registrationNumber,
                parallelImportId = parallelImportId,
                parallelImportSupplier = parallelImportSupplier,
                registrationProcess = registrationProcess,
                dailyDoseAmount = dailyDoseAmount,
                dailyDoseUnit = dailyDoseUnit,
                dailyDosePackaging = dailyDosePackaging,
                whoSource = whoSource,
                substanceList = substanceList,
                dispenseType = dispenseType,
                addictionCategory = addictionCategory,
                dopingCategory = dopingCategory,
                governmentRegulationCategory = governmentRegulationCategory,
                deliveriesFlag = deliveriesFlag,
                ean = ean,
                braille = braille,
                expiryPeriodDuration = expiryPeriodDuration,
                expiryPeriodUnit = expiryPeriodUnit,
                registeredName = registeredName,
                mrpNumber = mrpNumber,
                registrationLegalBasis = registrationLegalBasis,
                safetyFeature = safetyFeature,
                prescriptionRestriction = prescriptionRestriction,
                medicinalProductType = medicinalProductType,
                firstSeen = importedDatasetValidFrom,
                missingSince = null
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse ${getDatasetType().description} row: ${row.joinToString()}" }
            return null
        }
    }

    private fun parseDate(value: String): LocalDate? =
        value.takeIf { it.isNotBlank() }?.let {
            runCatching {
                LocalDate.parse(it, DateTimeFormatter.ofPattern("yyMMdd"))
            }.getOrNull()
        }

    private fun findOrganisation(code: String, countryCode: String): MpdOrganisation? =
        if (code.isNotBlank() && countryCode.isNotBlank())
            referenceDataProvider.getOrganisations()[code.trim() to countryCode.trim()]
        else null
}
