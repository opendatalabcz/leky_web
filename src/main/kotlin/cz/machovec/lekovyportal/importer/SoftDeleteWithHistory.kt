package cz.machovec.lekovyportal.importer

import cz.machovec.lekovyportal.domain.entity.mpd.BaseMpdEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

class SoftDeleteWithHistory<T : BaseMpdEntity<T>>(private val today: LocalDate = LocalDate.now()) {
    // TODO: refactor with more history
    fun apply(records: List<T>, repo: JpaRepository<T, Long>) {
        val existing = repo.findAll()
        val byKey = existing.associateBy { it.getUniqueKey() }
        val toSave = mutableListOf<T>()
        records.forEach { inc ->
            val cur = byKey[inc.getUniqueKey()]
            if (cur == null || cur.getBusinessAttributeChanges(inc).isNotEmpty() || cur.missingSince != null) {
                toSave += if (cur == null) inc else inc.copyPreservingIdAndFirstSeen(cur)
            }
        }
        val incomingKeys = records.map { it.getUniqueKey() }.toSet()
        val nowMissing = existing.filter { it.missingSince == null && it.getUniqueKey() !in incomingKeys }
            .map { it.markMissing(today) }
        repo.saveAll(toSave + nowMissing)
    }
}

