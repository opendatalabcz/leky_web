package cz.machovec.lekovyportal.core.domain.mpd

import cz.machovec.lekovyportal.core.domain.AttributeChange
import java.time.LocalDate

abstract class BaseMpdEntity<T : BaseMpdEntity<T>> {
    abstract val id: Long?
    abstract val firstSeen: LocalDate
    abstract val missingSince: LocalDate?

    abstract fun copyPreservingIdAndFirstSeen(from: T): T
    abstract fun markMissing(since: LocalDate): T
    abstract fun getUniqueKey(): String
    abstract fun getBusinessAttributeChanges(other: T): List<AttributeChange<*>>
}
