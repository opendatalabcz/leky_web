package cz.machovec.lekovyportal.processor.mdp

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import cz.machovec.lekovyportal.domain.entity.mpd.MpdAttributeChange
import cz.machovec.lekovyportal.domain.entity.mpd.MpdCountry
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdOrganisation
import cz.machovec.lekovyportal.domain.entity.mpd.MpdRecordTemporaryAbsence
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdOrganisationRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset
import java.time.LocalDate

/**
 * MpdOrganisationProcessor – import logic with 6 possible record states:
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
class MpdOrganisationProcessor(
    private val organisationRepository: MpdOrganisationRepository,
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

        val rows = reader.readAll().filter { row -> row.any { it.isNotBlank() } }

        val existingCountries = referenceDataProvider.getCountries()
        val importedRecords = rows.mapNotNull { parseLine(it, importedDatasetValidFrom, existingCountries) }
        val importedKeys = importedRecords.map { it.code to it.country.id }.toSet()

        val existingRecords = organisationRepository.findAll()
        val recordsToSave = mutableListOf<MpdOrganisation>()
        val attributeChangesToSave = mutableListOf<MpdAttributeChange>()
        val reactivationsToSave = mutableListOf<MpdRecordTemporaryAbsence>()

        importedRecords.forEach { imported ->
            val existing = existingRecords.find { it.code == imported.code && it.country.id == imported.country.id }

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
                            "Organisation ${existing.code} (${existing.country.code}) attribute '${it.attribute}' changed from '${it.oldValue}' to '${it.newValue}'"
                        }

                        attributeChangesToSave += MpdAttributeChange(
                            datasetType = MpdDatasetType.MPD_ORGANISATION,
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
                    logger.info { "Organisation ${existing.code} (${existing.country.code}) reactivated (validFrom ${imported.firstSeen})" }

                    reactivationsToSave += MpdRecordTemporaryAbsence(
                        datasetType = MpdDatasetType.MPD_ORGANISATION,
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
            !importedKeys.contains(it.code to it.country.id) && it.missingSince == null
        }

        newlyMissingRecords.forEach {
            // [5] Newly missing record
            recordsToSave += it.copy(missingSince = importedDatasetValidFrom)
            logger.info { "Organisation ${it.code} (${it.country.code}) marked invalid from $importedDatasetValidFrom" }
        }

        // [6] Already missing records → skipped

        organisationRepository.saveAll(recordsToSave)
        attributeChangeRepository.saveAll(attributeChangesToSave)
        temporaryAbsenceRepository.saveAll(reactivationsToSave)

        logger.info {
            """
            MpdOrganisation import summary:
              - Updated records: ${recordsToSave.size}
              - Attribute changes: ${attributeChangesToSave.size}
              - Reactivations: ${reactivationsToSave.size}
            """.trimIndent()
        }
    }

    private fun parseLine(
        cols: Array<String>,
        importedDatasetValidFrom: LocalDate,
        existingCountries: Map<String, MpdCountry>
    ): MpdOrganisation? {
        if (cols.size < 5) return null

        val code = cols[0].trim()
        val countryCode = cols[1].trim()
        val name = cols[2].trim()
        val isManufacturer = cols[3].trim().equals("V", ignoreCase = true)
        val isMarketingAuthHolder = cols[4].trim().equals("D", ignoreCase = true)

        val country = existingCountries[countryCode] ?: return null

        return MpdOrganisation(
            code = code,
            country = country,
            name = name,
            isManufacturer = isManufacturer,
            isMarketingAuthorizationHolder = isMarketingAuthHolder,
            firstSeen = importedDatasetValidFrom,
            missingSince = null
        )
    }
}
