package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProduct
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAddictionCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAdministrationRouteRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAtcGroupRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCountryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDispenseTypeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDopingCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDosageFormRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdGovernmentRegulationCategoryRepository
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
    private val governmentRegulationCategoryRepository: MpdGovernmentRegulationCategoryRepository
) {
    private var counter = 0

    @Transactional
    fun importData(csvBytes: ByteArray, validFromOfNewDataset: LocalDate, validToOfNewDataset: LocalDate?) {
        val text = csvBytes.toString(Charset.forName("Windows-1250")) // Správná konverze kódování
        val lines = text.split("\r\n", "\n").drop(1).filter { it.isNotBlank() }.take(100) // Zpracuje pouze prvních 5000 řádků

        val currentData = lines.mapNotNull { parseLine(it, validFromOfNewDataset) }
        val newCodes = currentData.map { it.suklCode }.toSet()
        val existingRecords = medicinalProductRepository.findAllBySuklCodeIn(newCodes)
        val updatedRecords = mutableListOf<MpdMedicinalProduct>()

        currentData.forEach { row ->
            val existing = existingRecords.find { it.suklCode == row.suklCode }
            if (existing == null) {
                updatedRecords += row
            } else {
                var changed = false

                if (existing.name != row.name || existing.strength != row.strength || existing.packaging != row.packaging) {
                    logger.info { "Product ${existing.suklCode} updated." }
                    changed = true
                }

                if (existing.validTo != null) {
                    logger.info { "Product ${existing.suklCode} reactivated (validFrom ${row.validFrom})" }
                    changed = true
                }

                if (changed) {
                    updatedRecords += existing.copy(
                        name = row.name,
                        strength = row.strength,
                        packaging = row.packaging,
                        validTo = null,
                        validFrom = row.validFrom
                    )
                }
            }
        }

        val missing = existingRecords.filter { !newCodes.contains(it.suklCode) && it.validTo == null }
        missing.forEach {
            updatedRecords += it.copy(validTo = validFromOfNewDataset)
            logger.info { "Product ${it.suklCode} marked invalid from $validFromOfNewDataset" }
        }

        medicinalProductRepository.saveAll(updatedRecords)
        logger.info { "Processed ${updatedRecords.size} updates for MpdMedicinalProduct." }
    }

    private fun parseLine(line: String, validFromOfNewDataset: LocalDate): MpdMedicinalProduct? {

        val cols = line.split(";")
        if (cols.size < 44) {
            println("Skipping line due to insufficient columns: ${cols.size}")
            return null
        }

        try {
            val suklCode = cols[0].trim()
            val name = cols[2].trim()
            val registeredName = cols[38].trim()
            val strength = cols[3].trim()
            val packaging = cols[5].trim()
            val supplement = cols[7].trim()
            val ean = cols[35].trim()
            val expiry = cols[36].trim()
            val expiryType = cols[37].trim()

            println("Basic fields extracted for suklCode: $suklCode")

            val atcGroup = atcGroupRepository.findByCode(cols[18].trim()) ?: return logAndReturnNull("ATC group", suklCode)
            val administrationRoute = administrationRouteRepository.findByCode(cols[6].trim()) ?: return logAndReturnNull("Administration route", suklCode)
            val dosageForm = dosageFormRepository.findByCode(cols[4].trim()) ?: return logAndReturnNull("Dosage form", suklCode)
            val packageType = packageTypeRepository.findByCode(cols[8].trim()) ?: return logAndReturnNull("Package type", suklCode)
            val marketingAuthorizationHolder = organisationRepository.findByCodeAndCountryCode(
                cols[9].trim(), cols[10].trim()
            ) ?: return logAndReturnNull("Marketing authorization holder ${cols[9]}, ${cols[10]}", suklCode)
            val country = countryRepository.findByCode(cols[10].trim()) ?: return logAndReturnNull("Country", suklCode)
            val registrationStatus = registrationStatusRepository.findByCode(cols[13].trim()) ?: return logAndReturnNull("Registration status ${cols[14]}", suklCode)
            val registrationProcess = registrationProcessRepository.findByCode(cols[23].trim()) ?: return logAndReturnNull("Registration process ${cols[24]}", suklCode)
            val dispenseType = dispenseTypeRepository.findByCode(cols[29].trim()) ?: return logAndReturnNull("Dispense type ${cols[30]}", suklCode)

            val addictionCategory = cols[30].takeIf { it.isNotEmpty() }?.let {
                addictionCategoryRepository.findByCode(it)
            }
            val dopingCategory = cols[31].takeIf { it.isNotEmpty() }?.let {
                dopingCategoryRepository.findByCode(it)
            }
            val governmentRegulationCategory = cols[32].takeIf { it.isNotEmpty() }?.let {
                governmentRegulationCategoryRepository.findByCode(it)
            }

            println("FK lookups passed for suklCode: $suklCode")

            return MpdMedicinalProduct(
                suklCode = suklCode,
                name = name,
                registeredName = registeredName,
                strength = strength,
                packaging = packaging,
                supplement = supplement,
                ean = ean,
                expiry = expiry,
                expiryType = expiryType,
                atcGroup = atcGroup,
                administrationRoute = administrationRoute,
                dosageForm = dosageForm,
                packageType = packageType,
                marketingAuthorizationHolder = marketingAuthorizationHolder,
                country = country,
                registrationStatus = registrationStatus,
                registrationProcess = registrationProcess,
                dispenseType = dispenseType,
                addictionCategory = addictionCategory,
                dopingCategory = dopingCategory,
                governmentRegulationCategory = governmentRegulationCategory,
                reportingObligation = cols[1] == "X",
                registrationNumber = cols[18].trim(),
                parallelImportId = cols[19].trim(),
                parallelImportSupplier = cols[20].trim(),
                parallelImportCountry = cols[21].takeIf { it.isNotEmpty() }?.let { countryRepository.findByCode(it) },
                dddAmount = cols[24].toBigDecimalOrNull(),
                dddUnit = cols[25].trim(),
                dddPackaging = cols[26].toBigDecimalOrNull(),
                whoSource = cols[27].trim(),
                substanceList = cols[28].trim(),
                deliveriesFlag = cols[33] == "X",
                brailleMarking = cols[35].trim(),
                mrpNumber = cols[38].trim(),
                legalBasis = cols[39].trim(),
                safetyFeature = cols[40] == "A",
                prescriptionRestriction = cols[41] == "A",
                medicinalProductType = cols[42].trim(),
                validFrom = validFromOfNewDataset,
                validTo = null
            )
        } catch (e: Exception) {
            println("Error processing line: $line")
            e.printStackTrace()
            return null
        }
    }

    // Helper function to log missing FK references
    private fun logAndReturnNull(field: String, suklCode: String): MpdMedicinalProduct? {
        println("Skipping suklCode: $suklCode due to missing $field")
        return null
    }

}
