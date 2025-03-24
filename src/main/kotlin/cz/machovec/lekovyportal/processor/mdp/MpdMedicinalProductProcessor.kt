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
        private const val COLUMN_SUKL_CODE = "KOD_SUKL"
        private const val COLUMN_REPORTING_OBLIGATION = "H"
        private const val COLUMN_NAME = "NAZEV"
        private const val COLUMN_STRENGTH = "SILA"
        private const val COLUMN_DOSAGE_FORM = "FORMA"
        private const val COLUMN_PACKAGING = "BALENI"
        private const val COLUMN_ADMIN_ROUTE = "CESTA"
        private const val COLUMN_SUPPLEMENTARY_INFO = "DOPLNEK"
        private const val COLUMN_PACKAGE_TYPE = "OBAL"
        private const val COLUMN_MAH_CODE = "DRZ"
        private const val COLUMN_MAH_COUNTRY = "ZEMDRZ"
        private const val COLUMN_CURR_MAH_CODE = "AKT_DRZ"
        private const val COLUMN_CURR_MAH_COUNTRY = "AKT_ZEM"
        private const val COLUMN_REGISTRATION_STATUS = "REG"
        private const val COLUMN_REGISTRATION_VALID_TO = "V_PLATDO"
        private const val COLUMN_REGISTRATION_UNLIMITED = "NEOMEZ"
        private const val COLUMN_MARKET_SUPPLY_END = "UVADENIDO"
        private const val COLUMN_INDICATION_GROUP = "IS_"
        private const val COLUMN_ATC_GROUP = "ATC_WHO"
        private const val COLUMN_REGISTRATION_NUMBER = "RC"
        private const val COLUMN_PARALLEL_IMPORT_ID = "SDOV"
        private const val COLUMN_PARALLEL_IMPORT_SUPPLIER_CODE = "SDOV_DOD"
        private const val COLUMN_PARALLEL_IMPORT_SUPPLIER_COUNTRY = "SDOV_ZEM"
        private const val COLUMN_REGISTRATION_PROCESS = "REG_PROC"
        private const val COLUMN_DAILY_DOSE_AMOUNT = "DDDAMNT_WHO"
        private const val COLUMN_DAILY_DOSE_UNIT = "DDDUN_WHO"
        private const val COLUMN_DAILY_DOSE_PACKAGING = "DDDP_WHO"
        private const val COLUMN_WHO_SOURCE = "ZDROJ_WHO"
        private const val COLUMN_SUBSTANCE_LIST = "LL"
        private const val COLUMN_DISPENSE_TYPE = "VYDEJ"
        private const val COLUMN_ADDICTION_CATEGORY = "ZAV"
        private const val COLUMN_DOPING_CATEGORY = "DOPING"
        private const val COLUMN_GOV_REG_CATEGORY = "NARVLA"
        private const val COLUMN_DELIVERIES_FLAG = "DODAVKY"
        private const val COLUMN_EAN = "EAN"
        private const val COLUMN_BRAILLE = "BRAILLOVO_PISMO"
        private const val COLUMN_EXPIRY_PERIOD_DURATION = "EXP"
        private const val COLUMN_EXPIRY_PERIOD_UNIT = "EXP_T"
        private const val COLUMN_REGISTERED_NAME = "NAZEV_REG"
        private const val COLUMN_MRP_NUMBER = "MRP_CISLO"
        private const val COLUMN_REGISTRATION_LEGAL_BASIS = "PRAVNI_ZAKLAD_REGISTRACE"
        private const val COLUMN_SAFETY_FEATURE = "OCHRANNY_PRVEK"
        private const val COLUMN_PRESCRIPTION_RESTRICTION = "OMEZENI_PRESKRIPCE_SMP"
        private const val COLUMN_MEDICINAL_PRODUCT_TYPE = "TYP_LP"
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
        try {
            val suklCode = headerIndex[COLUMN_SUKL_CODE]?.let { row.getOrNull(it)?.trim() }.orEmpty()
            if (suklCode.isBlank()) return null

            val reportingObligation = headerIndex[COLUMN_REPORTING_OBLIGATION]?.let {
                row.getOrNull(it)?.trim() == "X"
            } ?: false

            val name = headerIndex[COLUMN_NAME]?.let { row.getOrNull(it)?.trim() }.orEmpty()
            if (name.isBlank()) return null

            val strength = headerIndex[COLUMN_STRENGTH]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }
            val dosageForm = headerIndex[COLUMN_DOSAGE_FORM]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getDosageForms()[it] }
            val packaging = headerIndex[COLUMN_PACKAGING]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }
            val administrationRoute = headerIndex[COLUMN_ADMIN_ROUTE]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getAdministrationRoutes()[it] }
            val supplementaryInformation = headerIndex[COLUMN_SUPPLEMENTARY_INFO]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }
            val packageType = headerIndex[COLUMN_PACKAGE_TYPE]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getPackageTypes()[it] }

            val marketingAuthorizationHolder = headerIndex[COLUMN_MAH_CODE]?.let { codeIdx ->
                headerIndex[COLUMN_MAH_COUNTRY]?.let { countryIdx ->
                    val code = row.getOrNull(codeIdx)?.trim()
                    val country = row.getOrNull(countryIdx)?.trim()
                    if (!code.isNullOrBlank() && !country.isNullOrBlank()) findOrganisation(code, country) else null
                }
            }

            val currentMarketingAuthorizationHolder = headerIndex[COLUMN_CURR_MAH_CODE]?.let { codeIdx ->
                headerIndex[COLUMN_CURR_MAH_COUNTRY]?.let { countryIdx ->
                    val code = row.getOrNull(codeIdx)?.trim()
                    val country = row.getOrNull(countryIdx)?.trim()
                    if (!code.isNullOrBlank() && !country.isNullOrBlank()) findOrganisation(code, country) else null
                }
            }

            val registrationStatus = headerIndex[COLUMN_REGISTRATION_STATUS]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getRegistrationStatuses()[it] }
            val registrationValidTo = headerIndex[COLUMN_REGISTRATION_VALID_TO]?.let { parseDate(row.getOrNull(it)?.trim().orEmpty()) }
            val registrationUnlimited = headerIndex[COLUMN_REGISTRATION_UNLIMITED]?.let { row.getOrNull(it)?.trim() == "X" } ?: false
            val marketSupplyEndDate = headerIndex[COLUMN_MARKET_SUPPLY_END]?.let { parseDate(row.getOrNull(it)?.trim().orEmpty()) }

            val indicationGroup = headerIndex[COLUMN_INDICATION_GROUP]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getIndicationGroups()[it] }
            val atcGroup = headerIndex[COLUMN_ATC_GROUP]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getAtcGroups()[it] }

            val registrationNumber = headerIndex[COLUMN_REGISTRATION_NUMBER]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }
            val parallelImportId = headerIndex[COLUMN_PARALLEL_IMPORT_ID]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }

            val parallelImportSupplier = headerIndex[COLUMN_PARALLEL_IMPORT_SUPPLIER_CODE]?.let { codeIdx ->
                headerIndex[COLUMN_PARALLEL_IMPORT_SUPPLIER_COUNTRY]?.let { countryIdx ->
                    val code = row.getOrNull(codeIdx)?.trim()
                    val country = row.getOrNull(countryIdx)?.trim()
                    if (!code.isNullOrBlank() && !country.isNullOrBlank()) findOrganisation(code, country) else null
                }
            }

            val registrationProcess = headerIndex[COLUMN_REGISTRATION_PROCESS]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getRegistrationProcesses()[it] }
            val dailyDoseAmount = headerIndex[COLUMN_DAILY_DOSE_AMOUNT]?.let { row.getOrNull(it)?.trim()?.toBigDecimalOrNull() }
            val dailyDoseUnit = headerIndex[COLUMN_DAILY_DOSE_UNIT]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getMeasurementUnits()[it] }
            val dailyDosePackaging = headerIndex[COLUMN_DAILY_DOSE_PACKAGING]?.let { row.getOrNull(it)?.trim()?.toBigDecimalOrNull() }
            val whoSource = headerIndex[COLUMN_WHO_SOURCE]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }
            val substanceList = headerIndex[COLUMN_SUBSTANCE_LIST]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }

            val dispenseType = headerIndex[COLUMN_DISPENSE_TYPE]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getDispenseTypes()[it] }
            val addictionCategory = headerIndex[COLUMN_ADDICTION_CATEGORY]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getAddictionCategories()[it] }
            val dopingCategory = headerIndex[COLUMN_DOPING_CATEGORY]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getDopingCategories()[it] }
            val governmentRegulationCategory = headerIndex[COLUMN_GOV_REG_CATEGORY]?.let { row.getOrNull(it)?.trim() }?.let { referenceDataProvider.getGovRegulationCategories()[it] }

            val deliveriesFlag = headerIndex[COLUMN_DELIVERIES_FLAG]?.let { row.getOrNull(it)?.trim() == "X" } ?: false
            val ean = headerIndex[COLUMN_EAN]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }
            val braille = headerIndex[COLUMN_BRAILLE]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }
            val expiryPeriodDuration = headerIndex[COLUMN_EXPIRY_PERIOD_DURATION]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }
            val expiryPeriodUnit = headerIndex[COLUMN_EXPIRY_PERIOD_UNIT]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }
            val registeredName = headerIndex[COLUMN_REGISTERED_NAME]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }

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
