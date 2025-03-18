package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProduct
import cz.machovec.lekovyportal.domain.entity.mpd.MpdOrganisation
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAddictionCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAdministrationRouteRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAtcGroupRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCountryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDispenseTypeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDopingCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDosageFormRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdGovernmentRegulationCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdIndicationGroupRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMeasurementUnitRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMedicinalProductRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdOrganisationRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdPackageTypeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRegistrationProcessRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRegistrationStatusRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

@Service
class MpdMedicinalProductProcessor(
    private val medicinalProductRepository: MpdMedicinalProductRepository,
    private val atcGroupRepository: MpdAtcGroupRepository,
    private val administrationRouteRepository: MpdAdministrationRouteRepository,
    private val dosageFormRepository: MpdDosageFormRepository,
    private val packageTypeRepository: MpdPackageTypeRepository,
    private val organisationRepository: MpdOrganisationRepository,
    private val countryRepository: MpdCountryRepository,
    private val registrationStatusRepository: MpdRegistrationStatusRepository,
    private val registrationProcessRepository: MpdRegistrationProcessRepository,
    private val dispenseTypeRepository: MpdDispenseTypeRepository,
    private val addictionCategoryRepository: MpdAddictionCategoryRepository,
    private val dopingCategoryRepository: MpdDopingCategoryRepository,
    private val governmentRegulationCategoryRepository: MpdGovernmentRegulationCategoryRepository,
    private val indicationGroupRepository: MpdIndicationGroupRepository,
    private val measurementUnitRepository: MpdMeasurementUnitRepository
) {

    @Transactional
    fun importData(csvBytes: ByteArray, validFromOfNewDataset: LocalDate, validToOfNewDataset: LocalDate?) {
        val text = csvBytes.toString(Charset.forName("Windows-1250"))
        val lines = text.split("\r\n", "\n").drop(1).filter { it.isNotBlank() }.take(1000)

        val currentData = lines.mapNotNull { parseLine(it, validFromOfNewDataset) }
        val newCodes = currentData.map { it.suklCode }.toSet()
        val existingRecords = medicinalProductRepository.findAllBySuklCodeIn(newCodes)
        val updatedRecords = mutableListOf<MpdMedicinalProduct>()

        currentData.forEach { newRecord ->
            val existing = existingRecords.find { it.suklCode == newRecord.suklCode }
            if (existing == null) {
                updatedRecords += newRecord
            } else {
                var changed = false
                if (existing.name != newRecord.name || existing.strength != newRecord.strength || existing.packaging != newRecord.packaging) {
                    changed = true
                }
                if (existing.validTo != null) {
                    changed = true
                }

                if (changed) {
                    updatedRecords += existing.copy(
                        name = newRecord.name,
                        strength = newRecord.strength,
                        packaging = newRecord.packaging,
                        validTo = null,
                        validFrom = newRecord.validFrom
                    )
                }
            }
        }

        val missing = existingRecords.filter { !newCodes.contains(it.suklCode) && it.validTo == null }
        missing.forEach {
            updatedRecords += it.copy(validTo = validFromOfNewDataset)
        }

        medicinalProductRepository.saveAll(updatedRecords)

        logger.info { "Processed ${updatedRecords.size} updates for MpdMedicinalProduct." }
    }

    private fun parseLine(line: String, validFromOfNewDataset: LocalDate): MpdMedicinalProduct? {
        val cols = line.split(";")
        if (cols.size < 44) return null

        try {
            val suklCode = cols[0].trim()
            val reportingObligation = (cols[1].trim() == "X")
            val name = cols[2].trim()
            val strength = cols[3].trim().ifBlank { null }
            val packaging = cols[5].trim().ifBlank { null }
            val supplementaryInformation = cols[7].trim().ifBlank { null }

            val dosageForm = cols[4].trim().takeIf { it.isNotEmpty() }?.let { dosageFormRepository.findByCode(it) }
            val administrationRoute = cols[6].trim().takeIf { it.isNotEmpty() }?.let { administrationRouteRepository.findByCode(it) }
            val packageType = cols[8].trim().takeIf { it.isNotEmpty() }?.let { packageTypeRepository.findByCode(it) }

            val marketingAuthorizationHolder = findOrganisation(cols[9].trim(), cols[10].trim())
            val currentMarketingAuthorizationHolder = findOrganisation(cols[11].trim(), cols[12].trim())

            val registrationStatus = cols[13].trim().takeIf { it.isNotEmpty() }?.let { registrationStatusRepository.findByCode(it) }
            val registrationValidTo = parseDate(cols[14].trim())
            val registrationUnlimited = (cols[15].trim() == "X")
            val marketSupplyEndDate = parseDate(cols[16].trim())

            val indicationGroup = cols[17].trim().takeIf { it.isNotEmpty() }?.let { indicationGroupRepository.findByCode(it) }
            val atcGroup = cols[18].trim().takeIf { it.isNotEmpty() }?.let { atcGroupRepository.findByCode(it) }

            val registrationNumber = cols[19].trim().ifBlank { null }
            val parallelImportId = cols[20].trim().ifBlank { null }
            val parallelImportSupplier = findOrganisation(cols[21].trim(), cols[22].trim())

            val registrationProcess = cols[23].trim().takeIf { it.isNotEmpty() }?.let { registrationProcessRepository.findByCode(it) }
            val dailyDoseAmount = cols[24].trim().toBigDecimalOrNull()
            val dailyDoseUnit = cols[25].trim().takeIf { it.isNotEmpty() }?.let { measurementUnitRepository.findByCode(it) }
            val dailyDosePackaging = cols[26].trim().toBigDecimalOrNull()
            val whoSource = cols[27].trim().ifBlank { null }
            val substanceList = cols[28].trim().ifBlank { null }
            val dispenseType = cols[29].trim().takeIf { it.isNotEmpty() }?.let { dispenseTypeRepository.findByCode(it) }
            val addictionCategory = cols[30].trim().takeIf { it.isNotEmpty() }?.let { addictionCategoryRepository.findByCode(it) }
            val dopingCategory = cols[31].trim().takeIf { it.isNotEmpty() }?.let { dopingCategoryRepository.findByCode(it) }
            val governmentRegulationCategory = cols[32].trim().takeIf { it.isNotEmpty() }?.let { governmentRegulationCategoryRepository.findByCode(it) }

            val deliveriesFlag = (cols[33].trim() == "X")
            val ean = cols[34].trim().ifBlank { null }
            val braille = cols[35].trim().ifBlank { null }
            val expiryPeriodDuration = cols[36].trim().ifBlank { null }
            val expiryPeriodUnit = cols[37].trim().ifBlank { null }
            val registeredName = cols[38].trim().ifBlank { null }
            val mrpNumber = cols[39].trim().ifBlank { null }
            val registrationLegalBasis = cols[40].trim().ifBlank { null }
            val safetyFeature = (cols[41].trim() == "A")
            val prescriptionRestriction = (cols[42].trim() == "A")
            val medicinalProductType = cols[43].trim().ifBlank { null }

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
                validFrom = validFromOfNewDataset,
                validTo = null
            )
        } catch (e: Exception) {
            println("Error processing line: $line")
            e.printStackTrace()
            return null
        }
    }

    private fun findOrganisation(code: String, countryCode: String): MpdOrganisation? {
        return if (code.isNotEmpty() && countryCode.isNotEmpty()) {
            organisationRepository.findByCodeAndCountryCode(code, countryCode)
        } else null
    }

    private fun parseDate(raw: String): LocalDate? {
        return raw.takeIf { it.isNotEmpty() }?.let {
            LocalDate.parse(it, DateTimeFormatter.ofPattern("yyMMdd"))
        }
    }
}
