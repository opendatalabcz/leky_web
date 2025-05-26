package cz.machovec.lekovyportal.processor.processing.mpd

import cz.machovec.lekovyportal.core.domain.mpd.BaseMpdEntity
import cz.machovec.lekovyportal.core.domain.mpd.MpdDatasetType
import mu.KotlinLogging
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Synchronizes an MPD table with a freshly imported snapshot
 * and tracks history of attribute changes, temporary absences
 * and soft-deletes.
 */
@Component
class MpdEntitySynchronizer {

    private val log = KotlinLogging.logger {}

    /**
     * @param validFrom validity date of current dataset
     * @param dataset   logical dataset type
     * @param records   freshly imported entities
     * @param repo      JPA repository for the entity
     */
    fun <T> sync(
        validFrom: LocalDate,
        dataset: MpdDatasetType,
        records: List<T>,
        repo: JpaRepository<T, Long>
    ) where T : BaseMpdEntity<T> {

        val existing = repo.findAll()
        val existingByKey = existing.associateBy { it.getUniqueKey() }
        val incomingKeys = records.map { it.getUniqueKey() }.toSet()

        val entitiesToSave = mutableListOf<T>()

        var insertCount = 0
        var updateCount = 0

        records.forEach { incoming ->

            val current = existingByKey[incoming.getUniqueKey()]

            // [1] New record
            if (current == null) {
                entitiesToSave += incoming
                insertCount++
                return@forEach
            }

            val changes = current.getBusinessAttributeChanges(incoming)

            // [2] Reactivated or updated record — we no longer track absence separately
            if (current.missingSince != null || changes.isNotEmpty()) {
                entitiesToSave += incoming.copyPreservingIdAndFirstSeen(current)
                updateCount++
            }
            // else: no changes – skip
        }

        // [3] Newly missing
        val newlyMissing = existing
            .filter { it.missingSince == null && it.getUniqueKey() !in incomingKeys }
            .map { it.markMissing(validFrom) }

        // persist
        repo.saveAll(entitiesToSave + newlyMissing)

        // summary log
        log.info {
            "Synchronizer [$dataset]: " +
                    "insert=$insertCount, " +
                    "update=$updateCount, " +
                    "missing=${newlyMissing.size}"
        }
    }
}
