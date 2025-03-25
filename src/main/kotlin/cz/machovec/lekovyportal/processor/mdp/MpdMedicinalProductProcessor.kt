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
        private const val COLUMN_SUKL_CODE = "suklCode"
        private const val COLUMN_REPORTING_OBLIGATION = "reportingObligation"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_STRENGTH = "strength"
        private const val COLUMN_DOSAGE_FORM = "dosageForm"
        private const val COLUMN_PACKAGING = "packaging"
        private const val COLUMN_ADMIN_ROUTE = "administrationRoute"
        private const val COLUMN_SUPPLEMENTARY_INFO = "supplementaryInformation"
        private const val COLUMN_PACKAGE_TYPE = "packageType"
        private const val COLUMN_MAH_CODE = "mahCode"
        private const val COLUMN_MAH_COUNTRY = "mahCountry"
        private const val COLUMN_CURR_MAH_CODE = "currMahCode"
        private const val COLUMN_CURR_MAH_COUNTRY = "currMahCountry"
        private const val COLUMN_REGISTRATION_STATUS = "registrationStatus"
        private const val COLUMN_REGISTRATION_VALID_TO = "registrationValidTo"
        private const val COLUMN_REGISTRATION_UNLIMITED = "registrationUnlimited"
        private const val COLUMN_MARKET_SUPPLY_END = "marketSupplyEndDate"
        private const val COLUMN_INDICATION_GROUP = "indicationGroup"
        private const val COLUMN_ATC_GROUP = "atcGroup"
        private const val COLUMN_REGISTRATION_NUMBER = "registrationNumber"
        private const val COLUMN_PARALLEL_IMPORT_ID = "parallelImportId"
        private const val COLUMN_PARALLEL_IMPORT_SUPPLIER_CODE = "parallelImportSupplierCode"
        private const val COLUMN_PARALLEL_IMPORT_SUPPLIER_COUNTRY = "parallelImportSupplierCountry"
        private const val COLUMN_REGISTRATION_PROCESS = "registrationProcess"
        private const val COLUMN_DAILY_DOSE_AMOUNT = "dailyDoseAmount"
        private const val COLUMN_DAILY_DOSE_UNIT = "dailyDoseUnit"
        private const val COLUMN_DAILY_DOSE_PACKAGING = "dailyDosePackaging"
        private const val COLUMN_WHO_SOURCE = "whoSource"
        private const val COLUMN_SUBSTANCE_LIST = "substanceList"
        private const val COLUMN_DISPENSE_TYPE = "dispenseType"
        private const val COLUMN_ADDICTION_CATEGORY = "addictionCategory"
        private const val COLUMN_DOPING_CATEGORY = "dopingCategory"
        private const val COLUMN_GOV_REG_CATEGORY = "govRegulationCategory"
        private const val COLUMN_DELIVERIES_FLAG = "deliveriesFlag"
        private const val COLUMN_EAN = "ean"
        private const val COLUMN_BRAILLE = "braille"
        private const val COLUMN_EXPIRY_PERIOD_DURATION = "expiryPeriodDuration"
        private const val COLUMN_EXPIRY_PERIOD_UNIT = "expiryPeriodUnit"
        private const val COLUMN_REGISTERED_NAME = "registeredName"
        private const val COLUMN_MRP_NUMBER = "mrpNumber"
        private const val COLUMN_REGISTRATION_LEGAL_BASIS = "registrationLegalBasis"
        private const val COLUMN_SAFETY_FEATURE = "safetyFeature"
        private const val COLUMN_PRESCRIPTION_RESTRICTION = "prescriptionRestriction"
        private const val COLUMN_MEDICINAL_PRODUCT_TYPE = "medicinalProductType"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_MEDICINAL_PRODUCT

    override fun getExpectedColumnsMap(): Map<String, List<String>> = mapOf(
        COLUMN_SUKL_CODE to listOf("KOD_SUKL"),
        COLUMN_REPORTING_OBLIGATION to listOf("H"),
        COLUMN_NAME to listOf("NAZEV"),
        COLUMN_STRENGTH to listOf("SILA"),
        COLUMN_DOSAGE_FORM to listOf("FORMA"),
        COLUMN_PACKAGING to listOf("BALENI"),
        COLUMN_ADMIN_ROUTE to listOf("CESTA"),
        COLUMN_SUPPLEMENTARY_INFO to listOf("DOPLNEK"),
        COLUMN_PACKAGE_TYPE to listOf("OBAL"),
        COLUMN_MAH_CODE to listOf("DRZ"),
        COLUMN_MAH_COUNTRY to listOf("ZEMDRZ"),
        COLUMN_CURR_MAH_CODE to listOf("AKT_DRZ"),
        COLUMN_CURR_MAH_COUNTRY to listOf("AKT_ZEM"),
        COLUMN_REGISTRATION_STATUS to listOf("REG"),
        COLUMN_REGISTRATION_VALID_TO to listOf("V_PLATDO"),
        COLUMN_REGISTRATION_UNLIMITED to listOf("NEOMEZ"),
        COLUMN_MARKET_SUPPLY_END to listOf("UVADENIDO"),
        COLUMN_INDICATION_GROUP to listOf("IS_"),
        COLUMN_ATC_GROUP to listOf("ATC_WHO"),
        COLUMN_REGISTRATION_NUMBER to listOf("RC"),
        COLUMN_PARALLEL_IMPORT_ID to listOf("SDOV"),
        COLUMN_PARALLEL_IMPORT_SUPPLIER_CODE to listOf("SDOV_DOD"),
        COLUMN_PARALLEL_IMPORT_SUPPLIER_COUNTRY to listOf("SDOV_ZEM"),
        COLUMN_REGISTRATION_PROCESS to listOf("REG_PROC"),
        COLUMN_DAILY_DOSE_AMOUNT to listOf("DDDAMNT_WHO"),
        COLUMN_DAILY_DOSE_UNIT to listOf("DDDUN_WHO"),
        COLUMN_DAILY_DOSE_PACKAGING to listOf("DDDP_WHO"),
        COLUMN_WHO_SOURCE to listOf("ZDROJ_WHO"),
        COLUMN_SUBSTANCE_LIST to listOf("LL"),
        COLUMN_DISPENSE_TYPE to listOf("VYDEJ"),
        COLUMN_ADDICTION_CATEGORY to listOf("ZAV"),
        COLUMN_DOPING_CATEGORY to listOf("DOPING"),
        COLUMN_GOV_REG_CATEGORY to listOf("NARVLA"),
        COLUMN_DELIVERIES_FLAG to listOf("DODAVKY"),
        COLUMN_EAN to listOf("EAN"),
        COLUMN_BRAILLE to listOf("BRAILLOVO_PISMO"),
        COLUMN_EXPIRY_PERIOD_DURATION to listOf("EXP"),
        COLUMN_EXPIRY_PERIOD_UNIT to listOf("EXP_T"),
        COLUMN_REGISTERED_NAME to listOf("NAZEV_REG"),
        COLUMN_MRP_NUMBER to listOf("MRP_CISLO"),
        COLUMN_REGISTRATION_LEGAL_BASIS to listOf("PRAVNI_ZAKLAD_REGISTRACE"),
        COLUMN_SAFETY_FEATURE to listOf("OCHRANNY_PRVEK"),
        COLUMN_PRESCRIPTION_RESTRICTION to listOf("OMEZENI_PRESKRIPCE_SMP"),
        COLUMN_MEDICINAL_PRODUCT_TYPE to listOf("TYP_LP")
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
