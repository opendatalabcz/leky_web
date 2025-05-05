package cz.machovec.lekovyportal.importer.processing.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.BaseMpdEntity
import cz.machovec.lekovyportal.domain.entity.mpd.MpdAttributeChange
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdRecordTemporaryAbsence
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
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
class MpdEntitySynchronizer(
    private val attrRepo:    MpdAttributeChangeRepository,
    private val absenceRepo: MpdRecordTemporaryAbsenceRepository
) {

    private val log = KotlinLogging.logger {}

    /**
     * @param validFrom validity date of current dataset
     * @param dataset   logical dataset type
     * @param records   freshly imported entities
     * @param repo      JPA repository for the entity
     */
    fun <T> sync(
        validFrom: LocalDate,
        dataset:   MpdDatasetType,
        records:   List<T>,
        repo:      JpaRepository<T, Long>
    ) where T : BaseMpdEntity<T> {

        val existing         = repo.findAll()
        val existingByKey    = existing.associateBy { it.getUniqueKey() }
        val incomingKeys     = records.map { it.getUniqueKey() }.toSet()

        val entitiesToSave         = mutableListOf<T>()
        val attributeChangesToSave = mutableListOf<MpdAttributeChange>()
        val reactivationsToSave    = mutableListOf<MpdRecordTemporaryAbsence>()

        records.forEach { incoming ->

            val current = existingByKey[incoming.getUniqueKey()]

            /* ---------- [1] New record ---------- */
            if (current == null) {
                entitiesToSave += incoming
                return@forEach
            }

            val changes = current.getBusinessAttributeChanges(incoming)

            /* ---------- [2] Reactivated record ---------- */
            if (current.missingSince != null) {
                entitiesToSave += incoming.copyPreservingIdAndFirstSeen(current)

                // reactivation history
                reactivationsToSave += MpdRecordTemporaryAbsence(
                    datasetType = dataset,
                    recordId    = current.id!!,
                    missingFrom = current.missingSince!!,
                    missingTo   = validFrom.minusDays(1)
                )

                // attribute-change history (if anything really changed)
                if (changes.isNotEmpty()) {
                    attributeChangesToSave += changes.map {
                        MpdAttributeChange(
                            datasetType            = dataset,
                            recordId               = current.id!!,
                            attribute              = it.attribute,
                            oldValue               = it.oldValue?.toString(),
                            newValue               = it.newValue?.toString(),
                            seenInDatasetValidFrom = validFrom
                        )
                    }
                }
                return@forEach
            }

            /* ---------- [3] Attribute changes ---------- */
            if (changes.isNotEmpty()) {
                entitiesToSave += incoming.copyPreservingIdAndFirstSeen(current)

                attributeChangesToSave += changes.map {
                    MpdAttributeChange(
                        datasetType            = dataset,
                        recordId               = current.id!!,
                        attribute              = it.attribute,
                        oldValue               = it.oldValue?.toString(),
                        newValue               = it.newValue?.toString(),
                        seenInDatasetValidFrom = validFrom
                    )
                }
            }
            /* else: no changes – skip */
        }

        /* ---------- [4] Newly missing ---------- */
        val newlyMissing = existing
            .filter { it.missingSince == null && it.getUniqueKey() !in incomingKeys }
            .map { it.markMissing(validFrom) }

        /* ---------- persist ---------- */
        repo.saveAll(entitiesToSave + newlyMissing)
        attrRepo.saveAll(attributeChangesToSave)
        absenceRepo.saveAll(reactivationsToSave)

        /* ---------- summary ---------- */
        log.info {
            "Synchronizer [$dataset]: " +
                    "insert=${entitiesToSave.size - attributeChangesToSave.size - reactivationsToSave.size}, " +
                    "update=${attributeChangesToSave.size}, " +
                    "reactivated=${reactivationsToSave.size}, " +
                    "missing=${newlyMissing.size}"
        }
    }
}
