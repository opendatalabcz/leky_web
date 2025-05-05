package cz.machovec.lekovyportal.core.domain.mpd

import cz.machovec.lekovyportal.core.domain.AttributeChange
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "mpd_addiction_category")
data class MpdAddictionCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override val id: Long? = null,

    @Column(name = "first_seen", nullable = false)
    override val firstSeen: LocalDate,

    @Column(name = "missing_since")
    override val missingSince: LocalDate?,

    @Column(name = "code", nullable = false, unique = true)
    val code: String,

    @Column(name = "name")
    val name: String?
) : BaseMpdEntity<MpdAddictionCategory>() {

    override fun getUniqueKey(): String = code

    override fun copyPreservingIdAndFirstSeen(from: MpdAddictionCategory): MpdAddictionCategory {
        return this.copy(
            id = from.id,
            firstSeen = from.firstSeen,
            missingSince = null
        )
    }

    override fun markMissing(since: LocalDate): MpdAddictionCategory {
        return this.copy(missingSince = since)
    }

    override fun getBusinessAttributeChanges(other: MpdAddictionCategory): List<AttributeChange<*>> {
        val changes = mutableListOf<AttributeChange<*>>()
        fun <T> compare(attr: String, a: T?, b: T?) {
            if (a != b) changes += AttributeChange(attr, a, b)
        }

        compare("name", name, other.name)

        return changes
    }
}
