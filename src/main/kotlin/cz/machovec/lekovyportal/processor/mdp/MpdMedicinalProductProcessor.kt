package cz.machovec.lekovyportal.processor.mdp

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import cz.machovec.lekovyportal.domain.entity.mpd.MpdAttributeChange
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProduct
import cz.machovec.lekovyportal.domain.entity.mpd.MpdOrganisation
import cz.machovec.lekovyportal.domain.entity.mpd.MpdRecordTemporaryAbsence
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMedicinalProductRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * MpdMedicinalProductProcessor – import logic with 6 possible record states:
 *
 * [1] New record               – Not found in DB → insert.
 * [2] No changes               – Found in DB, no attribute changes, not missing → skip.
 * [3] Attribute changes        – Found in DB, business fields differ → update + log changes.
 * [4] Reactivated records      – Previously missing (missingSince != null), now present → restore + log downtime.
 * [5] Newly missing record     – Was present, not in current dataset, not yet marked missing → mark as missing.
 * [6] Already missing records  – Still missing and already marked as such → skip.
 */

private val logger = KotlinLogging.logger {}

@Service
class MpdMedicinalProductProcessor(
    private val medicinalProductRepository: MpdMedicinalProductRepository,
    private val attributeChangeRepository: MpdAttributeChangeRepository,
    private val temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository,
    private val referenceDataProvider: MpdReferenceDataProvider
) {

    @Transactional
    fun importData(csvBytes: ByteArray, importedDatasetValidFrom: LocalDate, validToOfNewDataset: LocalDate?) {
        val reader = CSVReaderBuilder(csvBytes.inputStream().reader(Charset.forName("Windows-1250")))
            .withSkipLines(1)
            .withCSVParser(CSVParserBuilder().withSeparator(';').build())
            .build()

        val importedRecords = reader.readAll()
            .filter { row -> row.any { it.isNotBlank() } }
            .mapNotNull { parseLine(it, importedDatasetValidFrom) }

        val importedKeys = importedRecords.map { it.suklCode }.toSet()
        val existingRecords = medicinalProductRepository.findAll()
        val recordsToSave = mutableListOf<MpdMedicinalProduct>()
        val attributeChangesToSave = mutableListOf<MpdAttributeChange>()
        val reactivationsToSave = mutableListOf<MpdRecordTemporaryAbsence>()

        importedRecords.forEach { imported ->
            val existing = existingRecords.find { it.suklCode == imported.suklCode }

            if (existing == null) {
                // [1] New record
                recordsToSave += imported
            } else {
                val changes = existing.getBusinessAttributeChanges(imported)
                val existingWasMarkedMissing = existing.missingSince != null

                if (changes.isEmpty() && !existingWasMarkedMissing) {
                    // [2] No changes
                    return@forEach
                }

                if (changes.isNotEmpty()) {
                    // [3] Attribute changes
                    changes.forEach {
                        logger.info {
                            "MP ${existing.suklCode} attribute '${it.attribute}' changed from '${it.oldValue}' to '${it.newValue}'"
                        }

                        attributeChangesToSave += MpdAttributeChange(
                            datasetType = MpdDatasetType.MPD_MEDICINAL_PRODUCT,
                            recordId = existing.id!!,
                            attribute = it.attribute,
                            oldValue = it.oldValue?.toString(),
                            newValue = it.newValue?.toString(),
                            seenInDatasetValidFrom = importedDatasetValidFrom
                        )
                    }
                }

                if (existingWasMarkedMissing) {
                    // [4] Reactivated record
                    logger.info { "MP ${existing.suklCode} reactivated (validFrom ${imported.firstSeen})" }

                    reactivationsToSave += MpdRecordTemporaryAbsence(
                        datasetType = MpdDatasetType.MPD_MEDICINAL_PRODUCT,
                        recordId = existing.id!!,
                        missingFrom = existing.missingSince!!,
                        missingTo = importedDatasetValidFrom.minusDays(1)
                    )
                }

                // [3], [4] Common update
                recordsToSave += imported.copy(
                    id = existing.id,
                    firstSeen = existing.firstSeen,
                    missingSince = null
                )
            }
        }

        val newlyMissingRecords = existingRecords.filter {
            !importedKeys.contains(it.suklCode) && it.missingSince == null
        }

        newlyMissingRecords.forEach {
            // [5] Newly missing record
            recordsToSave += it.copy(missingSince = importedDatasetValidFrom)
            logger.info { "MP ${it.suklCode} marked invalid from $importedDatasetValidFrom" }
        }

        // [6] Already missing records

        medicinalProductRepository.saveAll(recordsToSave)
        attributeChangeRepository.saveAll(attributeChangesToSave)
        temporaryAbsenceRepository.saveAll(reactivationsToSave)

        logger.info {
            """
            MpdMedicinalProduct import summary:
              - Updated records: ${recordsToSave.size}
              - Attribute changes: ${attributeChangesToSave.size}
              - Reactivations: ${reactivationsToSave.size}
            """.trimIndent()
        }
    }

    private fun parseLine(cols: Array<String>, datasetValidFrom: LocalDate): MpdMedicinalProduct? {
        if (cols.size < 44) return null

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
            val mrpNumber = cols[39].trim().ifBlank { null }
            val registrationLegalBasis = cols[40].trim().ifBlank { null }
            val safetyFeature = cols[41].trim() == "A"
            val prescriptionRestriction = cols[42].trim() == "A"
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
                firstSeen = datasetValidFrom,
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
