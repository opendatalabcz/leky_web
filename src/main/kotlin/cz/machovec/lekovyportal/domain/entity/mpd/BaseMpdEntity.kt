package cz.machovec.lekovyportal.domain.entity.mpd

import cz.machovec.lekovyportal.domain.AttributeChange
import java.time.LocalDate

abstract class BaseMpdEntity<T : BaseMpdEntity<T>> {
    abstract val id: Long?
    abstract val firstSeen: LocalDate
    abstract val missingSince: LocalDate?

    abstract fun copyPreservingIdAndFirstSeen(from: T): T
    abstract fun markMissing(since: LocalDate): T
    abstract fun getUniqueKey(): Any
    abstract fun getBusinessAttributeChanges(other: T): List<AttributeChange<*>>
}
