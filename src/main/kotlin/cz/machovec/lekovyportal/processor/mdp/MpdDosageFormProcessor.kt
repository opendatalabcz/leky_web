package cz.machovec.lekovyportal.processor.mdp

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import cz.machovec.lekovyportal.domain.entity.mpd.MpdAttributeChange
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDosageForm
import cz.machovec.lekovyportal.domain.entity.mpd.MpdRecordTemporaryAbsence
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDosageFormRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset
import java.time.LocalDate

/**
 * MpdDosageFormProcessor – import logic with 6 possible record states:
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
class MpdDosageFormProcessor(
    private val dosageFormRepository: MpdDosageFormRepository,
    private val attributeChangeRepository: MpdAttributeChangeRepository,
    private val temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository,
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

        val importedKeys = importedRecords.map { it.code }.toSet()
        val existingRecords = dosageFormRepository.findAll()
        val recordsToSave = mutableListOf<MpdDosageForm>()
        val attributeChangesToSave = mutableListOf<MpdAttributeChange>()
        val reactivationsToSave = mutableListOf<MpdRecordTemporaryAbsence>()

        importedRecords.forEach { imported ->
            val existing = existingRecords.find { it.code == imported.code }

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
                            "DosageForm ${existing.code} attribute '${it.attribute}' changed from '${it.oldValue}' to '${it.newValue}'"
                        }

                        attributeChangesToSave += MpdAttributeChange(
                            datasetType = MpdDatasetType.MPD_DOSAGE_FORM,
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
                    logger.info {
                        "DosageForm ${existing.code} reactivated (validFrom ${imported.firstSeen})"
                    }

                    reactivationsToSave += MpdRecordTemporaryAbsence(
                        datasetType = MpdDatasetType.MPD_DOSAGE_FORM,
                        recordId = existing.id!!,
                        missingFrom = existing.missingSince!!,
                        missingTo = importedDatasetValidFrom.minusDays(1)
                    )
                }

                // [3], [4] Common update
                recordsToSave += existing.copy(
                    name = imported.name,
                    nameEn = imported.nameEn,
                    nameLat = imported.nameLat,
                    isCannabis = imported.isCannabis,
                    edqmCode = imported.edqmCode,
                    firstSeen = imported.firstSeen,
                    missingSince = null
                )
            }
        }

        val newlyMissingRecords = existingRecords.filter {
            !importedKeys.contains(it.code) && it.missingSince == null
        }

        newlyMissingRecords.forEach {
            // [5] Newly missing record
            recordsToSave += it.copy(missingSince = importedDatasetValidFrom)
            logger.info { "DosageForm ${it.code} marked invalid from $importedDatasetValidFrom" }
        }

        // [6] Already missing records

        dosageFormRepository.saveAll(recordsToSave)
        attributeChangeRepository.saveAll(attributeChangesToSave)
        temporaryAbsenceRepository.saveAll(reactivationsToSave)

        logger.info {
            """
            MpdDosageForm import summary:
              - Updated records: ${recordsToSave.size}
              - Attribute changes: ${attributeChangesToSave.size}
              - Reactivations: ${reactivationsToSave.size}
            """.trimIndent()
        }
    }

    private fun parseLine(
        cols: Array<String>,
        importedDatasetValidFrom: LocalDate
    ): MpdDosageForm? {
        if (cols.size < 6) return null

        val code = cols[0].trim()
        val name = cols[1].trim()
        val nameEn = cols[2].trim()
        val nameLat = cols[3].trim()
        val isCannabis = cols[4].trim().equals("A", ignoreCase = true)
        val edqmCode = cols[5].takeIf { it.isNotEmpty() }?.trim()?.toLongOrNull()

        return MpdDosageForm(
            code = code,
            name = name,
            nameEn = nameEn,
            nameLat = nameLat,
            isCannabis = isCannabis,
            edqmCode = edqmCode,
            firstSeen = importedDatasetValidFrom,
            missingSince = null
        )
    }
}
