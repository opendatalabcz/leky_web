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
        logger.info {
            "Starting import of ${getDatasetType().description} from CSV. Validity: $importedDatasetValidFrom - ${importedDatasetValidTo ?: "∞"}"
        }

        val (headers, rows) = readCsv(csvBytes)
        val columnIndexMap = findColumnIndexes(headers)

        val expectedKeys = getExpectedColumnsMap().keys
        val missingColumns = expectedKeys.filterNot { it in columnIndexMap.keys }

        if (missingColumns.isNotEmpty()) {
            logger.warn { "Missing required columns for ${getDatasetType().description}: $missingColumns" }
        }

        val seenKeys = mutableSetOf<Any>()

        val importedCsvRows = rows
            .filter { row -> row.any { it.isNotBlank() } }
            .mapNotNull { row ->
                val entity = mapCsvRowToEntity(row, columnIndexMap, importedDatasetValidFrom)
                if (entity == null) return@mapNotNull null

                val key = entity.getUniqueKey()
                if (!seenKeys.add(key)) {
                    logger.warn { "Duplicate key '$key' encountered in CSV. Skipping row: ${row.joinToString()}" }
                    return@mapNotNull null
                }

                entity
            }

        logger.debug { "Successfully parsed CSV rows: ${importedCsvRows.size} out of ${rows.size}" }

        val existingRecords = repository.findAll()

        val changesResult = detectChanges(existingRecords, importedCsvRows, importedDatasetValidFrom)
        val missingResult = detectNewlyMissing(existingRecords, importedCsvRows, importedDatasetValidFrom)

        val allRecordsToSave = changesResult.recordsToSave + missingResult.newlyMissingRecords

        repository.saveAll(allRecordsToSave)
        attributeChangeRepository.saveAll(changesResult.attributeChanges)
        temporaryAbsenceRepository.saveAll(changesResult.reactivations)

        logDetailedSummary(changesResult, missingResult)
    }

    protected open fun readCsv(csvBytes: ByteArray): Pair<List<String>, List<Array<String>>> {
        val reader = CSVReaderBuilder(csvBytes.inputStream().reader(Charset.forName("Windows-1250")))
            .withCSVParser(CSVParserBuilder().withSeparator(';').build())
            .build()

        val lines = reader.readAll()
        if (lines.isEmpty()) return emptyList<String>() to emptyList()

        val headers = lines.first().map { it.trim() }
        val rows = lines.drop(1)
        return headers to rows
    }

    protected abstract fun getDatasetType(): MpdDatasetType

    protected abstract fun getExpectedColumnsMap(): Map<String, List<String>>

    protected abstract fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): T?

    private fun detectChanges(
        existingRecords: List<T>,
        importedRecords: List<T>,
        importedDatasetValidFrom: LocalDate
    ): ChangesResult<T> {
        val existingMap = existingRecords.associateBy { it.getUniqueKey() }

        val recordsToSave = mutableSetOf<T>()
        val attributeChanges = mutableListOf<MpdAttributeChange>()
        val reactivations = mutableListOf<MpdRecordTemporaryAbsence>()

        var newCount = 0
        var updatedCount = 0
        var reactivatedCount = 0
        var noChangeCount = 0

        importedRecords.forEach { imported ->
            val existing = existingMap[imported.getUniqueKey()]

            if (existing == null) {
                // [1] New record
                newCount++
                recordsToSave += imported
                return@forEach
            }

            val changes = existing.getBusinessAttributeChanges(imported)
            val wasMissing = existing.missingSince != null

            if (changes.isEmpty() && !wasMissing) {
                // [2] No changes
                noChangeCount++
                return@forEach
            }

            val merged = imported.copyPreservingIdAndFirstSeen(existing)
            recordsToSave += merged

            if (changes.isNotEmpty()) {
                // [3] Attribute changes
                updatedCount++
                changes.forEach {
                    logger.debug {
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

            if (wasMissing) {
                // [4] Reactivated record
                reactivatedCount++
                logger.debug {
                    "Key ${existing.getUniqueKey()} reactivated (validFrom ${imported.firstSeen})"
                }
                reactivations += MpdRecordTemporaryAbsence(
                    datasetType = getDatasetType(),
                    recordId = existing.id!!,
                    missingFrom = existing.missingSince!!,
                    missingTo = importedDatasetValidFrom.minusDays(1)
                )
            }
        }

        return ChangesResult(
            recordsToSave = recordsToSave.toList(),
            newCount = newCount,
            updatedCount = updatedCount,
            reactivatedCount = reactivatedCount,
            noChangeCount = noChangeCount,
            attributeChanges = attributeChanges,
            reactivations = reactivations
        )
    }

    private fun detectNewlyMissing(
        existingRecords: List<T>,
        importedRecords: List<T>,
        importedDatasetValidFrom: LocalDate
    ): MissingResult<T> {
        val importedKeys = importedRecords.map { it.getUniqueKey() }.toSet()

        // [5] Newly missing records
        val newlyMissing = existingRecords.filter {
            it.missingSince == null && !importedKeys.contains(it.getUniqueKey())
        }

        // [6] Already missing records
        val alreadyMissing = existingRecords.filter {
            it.missingSince != null && !importedKeys.contains(it.getUniqueKey())
        }

        val newlyMissingRecords = newlyMissing.map {
            logger.debug { "Key ${it.getUniqueKey()} marked missing since $importedDatasetValidFrom" }
            it.markMissing(importedDatasetValidFrom)
        }

        return MissingResult(
            newlyMissingRecords = newlyMissingRecords,
            alreadyMissingRecords = alreadyMissing
        )
    }

    private fun findColumnIndexes(headers: List<String>): Map<String, Int> {
        val normalizedHeaders = headers.mapIndexed { index, value ->
            index to value.trim().uppercase()
        }

        return getExpectedColumnsMap().mapNotNull { (englishName, aliases) ->
            val aliasUpper = aliases.map { it.trim().uppercase() }
            val index = normalizedHeaders.firstOrNull { (_, headerName) -> headerName in aliasUpper }?.first
            if (index != null) englishName to index else null
        }.toMap()
    }

    private fun logDetailedSummary(d: ChangesResult<T>, m: MissingResult<T>) {
        val summary = """
            ${getDatasetType().description} import summary:
              - New records: ${d.newCount}
              - Updated records: ${d.updatedCount}
              - Reactivated records: ${d.reactivatedCount}
              - Newly missing records: ${m.newlyMissingRecords.size}
              - Already missing records: ${m.alreadyMissingRecords.size}
              - No change records: ${d.noChangeCount}
              - Attribute changes logged: ${d.attributeChanges.size}
              - Reactivations logged: ${d.reactivations.size}
        """.trimIndent()

        logger.info { summary }
    }
}

data class ChangesResult<T>(
    val recordsToSave: List<T>,
    val newCount: Int,
    val updatedCount: Int,
    val reactivatedCount: Int,
    val noChangeCount: Int,
    val attributeChanges: List<MpdAttributeChange>,
    val reactivations: List<MpdRecordTemporaryAbsence>
)

data class MissingResult<T>(
    val newlyMissingRecords: List<T>,
    val alreadyMissingRecords: List<T>
)
