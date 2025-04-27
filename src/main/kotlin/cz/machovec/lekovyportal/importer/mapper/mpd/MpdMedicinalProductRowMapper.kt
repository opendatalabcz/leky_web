package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProduct
import cz.machovec.lekovyportal.importer.mapper.BaseRefRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
import cz.machovec.lekovyportal.importer.processing.mpd.MpdReferenceDataProvider
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class MpdMedicinalProductColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    SUKL_CODE(listOf("KOD_SUKL")),
    REPORTING_OBLIGATION(listOf("H")),
    NAME(listOf("NAZEV")),
    STRENGTH(listOf("SILA"), required = false),
    DOSAGE_FORM(listOf("FORMA"), required = false),
    PACKAGING(listOf("BALENI"), required = false),
    ADMINISTRATION_ROUTE(listOf("CESTA"), required = false),
    SUPPLEMENTARY_INFO(listOf("DOPLNEK"), required = false),
    PACKAGE_TYPE(listOf("OBAL"), required = false),
    MAH_CODE(listOf("DRZ"), required = false),
    MAH_COUNTRY(listOf("ZEMDRZ"), required = false),
    CURR_MAH_CODE(listOf("AKT_DRZ"), required = false),
    CURR_MAH_COUNTRY(listOf("AKT_ZEM"), required = false),
    REGISTRATION_STATUS(listOf("REG"), required = false),
    REGISTRATION_VALID_TO(listOf("V_PLATDO"), required = false),
    REGISTRATION_UNLIMITED(listOf("NEOMEZ"), required = false),
    MARKET_SUPPLY_END(listOf("UVADENIDO"), required = false),
    INDICATION_GROUP(listOf("IS_"), required = false),
    ATC_GROUP(listOf("ATC_WHO"), required = false),
    REGISTRATION_NUMBER(listOf("RC"), required = false),
    PARALLEL_IMPORT_ID(listOf("SDOV"), required = false),
    PARALLEL_IMPORT_SUPPLIER_CODE(listOf("SDOV_DOV", "SDOV_DOD"), required = false),
    PARALLEL_IMPORT_SUPPLIER_COUNTRY(listOf("SDOV_ZEM"), required = false),
    REGISTRATION_PROCESS(listOf("REG_PROC"), required = false),
    DAILY_DOSE_AMOUNT(listOf("DDDAMNT_WHO"), required = false),
    DAILY_DOSE_UNIT(listOf("DDDUN_WHO"), required = false),
    DAILY_DOSE_PACKAGING(listOf("DDDP_WHO"), required = false),
    WHO_SOURCE(listOf("ZDROJ_WHO"), required = false),
    SUBSTANCE_LIST(listOf("LL"), required = false),
    DISPENSE_TYPE(listOf("VYDEJ"), required = false),
    ADDICTION_CATEGORY(listOf("ZAV"), required = false),
    DOPING_CATEGORY(listOf("DOPING"), required = false),
    GOV_REG_CATEGORY(listOf("NARVLA"), required = false),
    DELIVERIES_FLAG(listOf("DODAVKY"), required = false),
    EAN(listOf("EAN"), required = false),
    BRAILLE(listOf("BRAILLOVO_PISMO"), required = false),
    EXPIRY_PERIOD_DURATION(listOf("EXP"), required = false),
    EXPIRY_PERIOD_UNIT(listOf("EXP_T"), required = false),
    REGISTERED_NAME(listOf("NAZEV_REG"), required = false),
    MRP_NUMBER(listOf("MRP_CISLO"), required = false),
    REGISTRATION_LEGAL_BASIS(listOf("PRAVNI_ZAKLAD_REGISTRACE"), required = false),
    SAFETY_FEATURE(listOf("OCHRANNY_PRVEK"), required = false),
    PRESCRIPTION_RESTRICTION(listOf("OMEZENI_PRESKRIPCE_SMP"), required = false),
    MEDICINAL_PRODUCT_TYPE(listOf("TYP_LP"), required = false);
}

class MpdMedicinalProductRowMapper(
    private val validFrom: LocalDate,
    refProvider: MpdReferenceDataProvider
) : BaseRefRowMapper<MpdMedicinalProductColumn, MpdMedicinalProduct>(refProvider) {

    override fun map(row: CsvRow<MpdMedicinalProductColumn>, rawLine: String): RowMappingResult<MpdMedicinalProduct> {

        /* ---------- mandatory attributes ---------- */
        val suklCode = row[MpdMedicinalProductColumn.SUKL_CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdMedicinalProductColumn.SUKL_CODE.name, rawLine)
            )
        val name = row[MpdMedicinalProductColumn.NAME].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdMedicinalProductColumn.NAME.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val reportingObligation = row[MpdMedicinalProductColumn.REPORTING_OBLIGATION]?.trim() == "X"
        val strength = row[MpdMedicinalProductColumn.STRENGTH].safeTrim()
        val dosageForm = row[MpdMedicinalProductColumn.DOSAGE_FORM]?.trim()?.let { ref.getDosageForms()[it] }
        val packaging = row[MpdMedicinalProductColumn.PACKAGING].safeTrim()
        val administrationRoute = row[MpdMedicinalProductColumn.ADMINISTRATION_ROUTE]?.trim()?.let { ref.getAdministrationRoutes()[it] }
        val supplementaryInfo = row[MpdMedicinalProductColumn.SUPPLEMENTARY_INFO].safeTrim()
        val packageType = row[MpdMedicinalProductColumn.PACKAGE_TYPE]?.trim()?.let { ref.getPackageTypes()[it] }

        val mahCode = row[MpdMedicinalProductColumn.MAH_CODE]?.trim()
        val mahCountry = row[MpdMedicinalProductColumn.MAH_COUNTRY]?.trim()
        val marketingAuthorizationHolder = mahCode?.takeIf { it.isNotBlank() }?.let { code ->
            mahCountry?.takeIf { it.isNotBlank() }?.let { country ->
                ref.getOrganisations()[code to country]
            }
        }

        val currMahCode = row[MpdMedicinalProductColumn.CURR_MAH_CODE]?.trim()
        val currMahCountry = row[MpdMedicinalProductColumn.CURR_MAH_COUNTRY]?.trim()
        val currentMarketingAuthorizationHolder = currMahCode?.takeIf { it.isNotBlank() }?.let { code ->
            currMahCountry?.takeIf { it.isNotBlank() }?.let { country ->
                ref.getOrganisations()[code to country]
            }
        }

        val registrationStatus = row[MpdMedicinalProductColumn.REGISTRATION_STATUS]?.trim()?.let { ref.getRegistrationStatuses()[it] }
        val registrationValidTo = row[MpdMedicinalProductColumn.REGISTRATION_VALID_TO]?.trim()?.let { parseDate(it) }
        val registrationUnlimited = row[MpdMedicinalProductColumn.REGISTRATION_UNLIMITED]?.trim() == "X"
        val marketSupplyEndDate = row[MpdMedicinalProductColumn.MARKET_SUPPLY_END]?.trim()?.let { parseDate(it) }
        val indicationGroup = row[MpdMedicinalProductColumn.INDICATION_GROUP]?.trim()?.let { ref.getIndicationGroups()[it] }
        val atcGroup = row[MpdMedicinalProductColumn.ATC_GROUP]?.trim()?.let { ref.getAtcGroups()[it] }
        val registrationNumber = row[MpdMedicinalProductColumn.REGISTRATION_NUMBER].safeTrim()
        val parallelImportId = row[MpdMedicinalProductColumn.PARALLEL_IMPORT_ID].safeTrim()

        val parallelImportSupplier = row[MpdMedicinalProductColumn.PARALLEL_IMPORT_SUPPLIER_CODE]?.trim()?.let { code ->
            row[MpdMedicinalProductColumn.PARALLEL_IMPORT_SUPPLIER_COUNTRY]?.trim()?.let { country ->
                ref.getOrganisations()[code to country]
            }
        }

        val registrationProcess = row[MpdMedicinalProductColumn.REGISTRATION_PROCESS]?.trim()?.let { ref.getRegistrationProcesses()[it] }
        val dailyDoseAmount = row[MpdMedicinalProductColumn.DAILY_DOSE_AMOUNT]?.parseDecimal()
        val dailyDoseUnit = row[MpdMedicinalProductColumn.DAILY_DOSE_UNIT]?.trim()?.let { ref.getMeasurementUnits()[it] }
        val dailyDosePackaging = row[MpdMedicinalProductColumn.DAILY_DOSE_PACKAGING]?.parseDecimal()
        val whoSource = row[MpdMedicinalProductColumn.WHO_SOURCE].safeTrim()
        val substanceList = row[MpdMedicinalProductColumn.SUBSTANCE_LIST].safeTrim()

        val dispenseType = row[MpdMedicinalProductColumn.DISPENSE_TYPE]?.trim()?.let { ref.getDispenseTypes()[it] }
        val addictionCategory = row[MpdMedicinalProductColumn.ADDICTION_CATEGORY]?.trim()?.let { ref.getAddictionCategories()[it] }
        val dopingCategory = row[MpdMedicinalProductColumn.DOPING_CATEGORY]?.trim()?.let { ref.getDopingCategories()[it] }
        val governmentRegulationCategory = row[MpdMedicinalProductColumn.GOV_REG_CATEGORY]?.trim()?.let { ref.getGovRegulationCategories()[it] }

        val deliveriesFlag = row[MpdMedicinalProductColumn.DELIVERIES_FLAG]?.trim() == "X"
        val ean = row[MpdMedicinalProductColumn.EAN].safeTrim()
        val braille = row[MpdMedicinalProductColumn.BRAILLE].safeTrim()
        val expiryPeriodDuration = row[MpdMedicinalProductColumn.EXPIRY_PERIOD_DURATION].safeTrim()
        val expiryPeriodUnit = row[MpdMedicinalProductColumn.EXPIRY_PERIOD_UNIT].safeTrim()
        val registeredName = row[MpdMedicinalProductColumn.REGISTERED_NAME].safeTrim()
        val mrpNumber = row[MpdMedicinalProductColumn.MRP_NUMBER].safeTrim()
        val registrationLegalBasis = row[MpdMedicinalProductColumn.REGISTRATION_LEGAL_BASIS].safeTrim()
        val safetyFeature = row[MpdMedicinalProductColumn.SAFETY_FEATURE]?.trim() == "A"
        val prescriptionRestriction = row[MpdMedicinalProductColumn.PRESCRIPTION_RESTRICTION]?.trim() == "A"
        val medicinalProductType = row[MpdMedicinalProductColumn.MEDICINAL_PRODUCT_TYPE].safeTrim()

        /* ---------- entity construction ---------- */
        val entity = MpdMedicinalProduct(
            id = null,
            firstSeen = validFrom,
            missingSince = null,
            suklCode = suklCode,
            reportingObligation = reportingObligation,
            name = name,
            strength = strength,
            dosageForm = dosageForm,
            packaging = packaging,
            administrationRoute = administrationRoute,
            supplementaryInformation = supplementaryInfo,
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
            medicinalProductType = medicinalProductType
        )

        return RowMappingResult.Success(entity)
    }

    private fun parseDate(value: String): LocalDate? =
        value.takeIf { it.isNotBlank() }?.let {
            runCatching {
                LocalDate.parse(it, DateTimeFormatter.ofPattern("yyMMdd"))
            }.getOrNull()
        }

    private fun String?.parseDecimal(): BigDecimal? =
        this?.trim()
            ?.replace(",", ".")
            ?.toBigDecimalOrNull()
}
