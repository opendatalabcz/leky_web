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
    override fun getDatasetType(): MpdDatasetType =
        MpdDatasetType.MPD_MEDICINAL_PRODUCT

    override fun mapCsvRowToEntity(cols: Array<String>, importedDatasetValidFrom: LocalDate): MpdMedicinalProduct? {
        if (cols.size < 39) {
            logger.warn { "Row skipped, expected at least 43 columns, but got ${cols.size}: ${cols.joinToString()}" }
            return null
        }

        try {
            val suklCode = cols[0].trim()
            val reportingObligation = cols[1].trim() == "X"
            val name = cols[2].trim()
            val strength = cols[3].trim().ifBlank { null }
            val dosageForm = cols[4].trim().takeIf { it.isNotEmpty() }?.let { referenceDataProvider.getDosageForms()[it] }
            val packaging = cols[5].trim().ifBlank { null }
            val administrationRoute = cols[6].trim().takeIf { it.isNotEmpty() }?.let { referenceDataProvider.getAdministrationRoutes()[it] }
            val supplementaryInformation = cols[7].trim().ifBlank { null }
            val packageType = cols[8].trim().takeIf { it.isNotEmpty() }?.let { referenceDataProvider.getPackageTypes()[it] }

            val marketingAuthorizationHolder = findOrganisation(cols[9], cols[10])
            val currentMarketingAuthorizationHolder = findOrganisation(cols[11], cols[12])

            val registrationStatus = cols[13].trim().takeIf { it.isNotEmpty() }?.let { referenceDataProvider.getRegistrationStatuses()[it] }
            val registrationValidTo = parseDate(cols[14].trim())
            val registrationUnlimited = cols[15].trim() == "X"
            val marketSupplyEndDate = parseDate(cols[16].trim())

            val indicationGroup = cols[17].trim().takeIf { it.isNotEmpty() }?.let { referenceDataProvider.getIndicationGroups()[it] }
            val atcGroup = cols[18].trim().takeIf { it.isNotEmpty() }?.let { referenceDataProvider.getAtcGroups()[it] }

            val registrationNumber = cols[19].trim().ifBlank { null }
            val parallelImportId = cols[20].trim().ifBlank { null }
            val parallelImportSupplier = findOrganisation(cols[21], cols[22])

            val registrationProcess = cols[23].trim().takeIf { it.isNotEmpty() }?.let { referenceDataProvider.getRegistrationProcesses()[it] }
            val dailyDoseAmount = cols[24].trim().toBigDecimalOrNull()
            val dailyDoseUnit = cols[25].trim().takeIf { it.isNotEmpty() }?.let { referenceDataProvider.getMeasurementUnits()[it] }
            val dailyDosePackaging = cols[26].trim().toBigDecimalOrNull()
            val whoSource = cols[27].trim().ifBlank { null }
            val substanceList = cols[28].trim().ifBlank { null }
            val dispenseType = cols[29].trim().takeIf { it.isNotEmpty() }?.let { referenceDataProvider.getDispenseTypes()[it] }
            val addictionCategory = cols[30].trim().takeIf { it.isNotEmpty() }?.let { referenceDataProvider.getAddictionCategories()[it] }
            val dopingCategory = cols[31].trim().takeIf { it.isNotEmpty() }?.let { referenceDataProvider.getDopingCategories()[it] }
            val governmentRegulationCategory = cols[32].trim().takeIf { it.isNotEmpty() }?.let { referenceDataProvider.getGovRegulationCategories()[it] }
            val deliveriesFlag = cols[33].trim() == "X"
            val ean = cols[34].trim().ifBlank { null }
            val braille = cols[35].trim().ifBlank { null }
            val expiryPeriodDuration = cols[36].trim().ifBlank { null }
            val expiryPeriodUnit = cols[37].trim().ifBlank { null }
            val registeredName = cols[38].trim().ifBlank { null }

            val mrpNumber = cols.getOrNull(39)?.trim()?.ifBlank { null }
            val registrationLegalBasis = cols.getOrNull(40)?.trim()?.ifBlank { null }
            val safetyFeature = cols.getOrNull(41)?.trim() == "A"
            val prescriptionRestriction = cols.getOrNull(42)?.trim() == "A"
            val medicinalProductType = cols.getOrNull(43)?.trim()?.ifBlank { null }

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
            logger.error(e) { "Failed to parse medicinal product row: ${cols.joinToString()}" }
            return null
        }
    }

    private fun parseDate(value: String): LocalDate? =
        value.takeIf { it.isNotBlank() }?.let {
            LocalDate.parse(it, DateTimeFormatter.ofPattern("yyMMdd"))
        }

    private fun findOrganisation(code: String, countryCode: String): MpdOrganisation? =
        if (code.isNotBlank() && countryCode.isNotBlank())
            referenceDataProvider.getOrganisations()[code.trim() to countryCode.trim()]
        else null
}
