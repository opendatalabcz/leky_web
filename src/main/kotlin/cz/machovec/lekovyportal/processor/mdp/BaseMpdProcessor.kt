package cz.machovec.lekovyportal.processor.mdp

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import cz.machovec.lekovyportal.domain.entity.mpd.BaseMpdEntity
import cz.machovec.lekovyportal.domain.entity.mpd.MpdAttributeChange
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdRecordTemporaryAbsence
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset
import java.time.LocalDate

/**
 * Abstract processor for MPD entities with 6 possible cases:
 *
 * [1] New record               – Not found in DB → insert.
 * [2] No changes               – Found in DB, no attribute changes, not missing → skip.
 * [3] Attribute changes        – Found in DB, business attributes differ → update + log changes.
 * [4] Reactivated records      – Previously missing (missingSince != null), now present → restore + log downtime.
 * [5] Newly missing record     – Was present, not in current dataset, not yet marked missing → mark as missing.
 * [6] Already missing records  – Still missing and already marked as such → skip.
 */

private val logger = KotlinLogging.logger {}

abstract class BaseMpdProcessor<T : BaseMpdEntity<T>>(
    private val repository: JpaRepository<T, Long>,
    private val attributeChangeRepository: MpdAttributeChangeRepository,
    private val temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository
) {

    @Transactional
    open fun processCsv(
        csvBytes: ByteArray,
        importedDatasetValidFrom: LocalDate,
        importedDatasetValidTo: LocalDate?
    ) {
        val importedCsvRows = readCsv(csvBytes)
            .filter { row -> row.any { it.isNotBlank() } }
            .mapNotNull { mapCsvRowToEntity(it, importedDatasetValidFrom) }

        val existingRecords = repository.findAll()

        val detectChangesResult = detectChanges(existingRecords, importedCsvRows, importedDatasetValidFrom)

        val missingResult = detectNewlyMissing(existingRecords, importedCsvRows, importedDatasetValidFrom)

        val recordsToSave =
                    detectChangesResult.newRecords +
                    detectChangesResult.updatedRecords +
                    detectChangesResult.reactivatedRecords +
                    missingResult.newlyMissingRecords

        repository.saveAll(recordsToSave)
        attributeChangeRepository.saveAll(detectChangesResult.attributeChanges)
        temporaryAbsenceRepository.saveAll(detectChangesResult.reactivations)

        logDetailedSummary(detectChangesResult, missingResult)
    }

    protected open fun readCsv(csvBytes: ByteArray): List<Array<String>> {
        val reader = CSVReaderBuilder(csvBytes.inputStream().reader(Charset.forName("Windows-1250")))
            .withSkipLines(1)
            .withCSVParser(CSVParserBuilder().withSeparator(';').build())
            .build()
        return reader.readAll()
    }

    protected abstract fun getDatasetType(): MpdDatasetType

    protected abstract fun mapCsvRowToEntity(cols: Array<String>, importedDatasetValidFrom: LocalDate): T?

    /**
     * [1], [2], [3], [4]
     */
    private fun detectChanges(
        existingRecords: List<T>,
        importedRecords: List<T>,
        importedDatasetValidFrom: LocalDate
    ): DetectionResult<T> {
        val newRecords = mutableListOf<T>()
        val updatedRecords = mutableListOf<T>()
        val reactivatedRecords = mutableListOf<T>()
        val noChangeRecords = mutableListOf<T>()

        val attributeChanges = mutableListOf<MpdAttributeChange>()
        val reactivations = mutableListOf<MpdRecordTemporaryAbsence>()

        val existingMap = existingRecords.associateBy { it.getUniqueKey() }

        importedRecords.forEach { imported ->
            val existing = existingMap[imported.getUniqueKey()]

            if (existing == null) {
                // [1] New record
                newRecords += imported
            } else {
                val changes = existing.getBusinessAttributeChanges(imported)
                val existingWasMarkedMissing = existing.missingSince != null

                if (changes.isEmpty() && !existingWasMarkedMissing) {
                    // [2] No changes
                    noChangeRecords += imported
                    return@forEach
                }

                if (changes.isNotEmpty()) {
                    // [3] Attribute changes
                    updatedRecords += imported
                    changes.forEach {
                        logger.info {
                            "Key ${existing.getUniqueKey()} attribute '${it.attribute}' changed from '${it.oldValue}' to '${it.newValue}'"
                        }
                        attributeChanges += MpdAttributeChange(
                            datasetType = getDatasetType(),
                            recordId = existing.id!!,
                            attribute = it.attribute,
                            oldValue = it.oldValue?.toString(),
                            newValue = it.newValue?.toString(),
                            seenInDatasetValidFrom = importedDatasetValidFrom
                        )
                    }
                }

                if (existingWasMarkedMissing) {
                    // [4] Reactivated records
                    reactivatedRecords += imported
                    logger.info { "Key ${existing.getUniqueKey()} reactivated (validFrom ${imported.firstSeen})" }
                    reactivations += MpdRecordTemporaryAbsence(
                        datasetType = getDatasetType(),
                        recordId = existing.id!!,
                        missingFrom = existing.missingSince!!,
                        missingTo = importedDatasetValidFrom.minusDays(1)
                    )
                }

                // [3], [4] Common update
                imported.copyPreservingIdAndFirstSeen(existing)
            }
        }

        return DetectionResult(
            newRecords = newRecords,
            updatedRecords = updatedRecords,
            reactivatedRecords = reactivatedRecords,
            noChangeRecords = noChangeRecords,
            attributeChanges = attributeChanges,
            reactivations = reactivations
        )
    }

    /**
     * [5], [6]
     */
    private fun detectNewlyMissing(
        existingRecords: List<T>,
        importedRecords: List<T>,
        importedDatasetValidFrom: LocalDate
    ): MissingResult<T> {
        val importedKeys = importedRecords.map { it.getUniqueKey() }.toSet()

        val newlyMissing = existingRecords.filter {
            it.missingSince == null && !importedKeys.contains(it.getUniqueKey())
        }

        val newlyMissingCopied = newlyMissing.map {
            logger.info { "Key ${it.getUniqueKey()} marked invalid from $importedDatasetValidFrom" }
            it.markMissing(importedDatasetValidFrom)
        }

        val alreadyMissing = existingRecords.filter {
            it.missingSince != null && !importedKeys.contains(it.getUniqueKey())
        }

        return MissingResult(
            newlyMissingRecords = newlyMissingCopied,
            alreadyMissingRecords = alreadyMissing
        )
    }

    private fun logDetailedSummary(detection: DetectionResult<T>, missingResult: MissingResult<T>) {
        val summary = """
            ${getDatasetType()} import summary:
              - New records: ${detection.newRecords.size}
              - Updated records (attribute changes): ${detection.updatedRecords.size}
              - Reactivated records: ${detection.reactivatedRecords.size}
              - Newly missing records: ${missingResult.newlyMissingRecords.size}
              - Already missing records: ${missingResult.alreadyMissingRecords.size}
              - No change records: ${detection.noChangeRecords.size}
              - Attribute changes: ${detection.attributeChanges.size}
              - Reactivations: ${detection.reactivations.size}
        """.trimIndent()

        logger.info { summary }
    }
}

data class DetectionResult<T>(
    val newRecords: List<T> = emptyList(),
    val updatedRecords: List<T> = emptyList(),
    val reactivatedRecords: List<T> = emptyList(),
    val noChangeRecords: List<T> = emptyList(),
    val attributeChanges: List<MpdAttributeChange> = emptyList(),
    val reactivations: List<MpdRecordTemporaryAbsence> = emptyList()
)

data class MissingResult<T>(
    val newlyMissingRecords: List<T> = emptyList(),
    val alreadyMissingRecords: List<T> = emptyList()
)
