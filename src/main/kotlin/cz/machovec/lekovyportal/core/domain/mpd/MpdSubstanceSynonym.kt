package cz.machovec.lekovyportal.core.domain.mpd

import cz.machovec.lekovyportal.core.domain.AttributeChange
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

@Entity
@Table(
    name = "mpd_substance_synonym",
    uniqueConstraints = [UniqueConstraint(columnNames = ["substance_id", "sequence_number", "source_id"])]
)
data class MpdSubstanceSynonym(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override val id: Long? = null,

    @Column(name = "first_seen", nullable = false)
    override val firstSeen: LocalDate,

    @Column(name = "missing_since")
    override val missingSince: LocalDate?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substance_id", nullable = false)
    val substance: MpdSubstance,

    @Column(name = "sequence_number")
    val sequenceNumber: Int?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    val source: MpdSource,

    @Column(name = "name")
    val name: String?
) : BaseMpdEntity<MpdSubstanceSynonym>() {

    override fun getUniqueKey(): String {
        return "${substance.id}-${sequenceNumber ?: "null"}-${source.id}"
    }

    override fun copyPreservingIdAndFirstSeen(from: MpdSubstanceSynonym): MpdSubstanceSynonym {
        return this.copy(
            id = from.id,
            firstSeen = from.firstSeen,
            missingSince = null
        )
    }

    override fun markMissing(since: LocalDate): MpdSubstanceSynonym {
        return this.copy(missingSince = since)
    }

    override fun getBusinessAttributeChanges(other: MpdSubstanceSynonym): List<AttributeChange<*>> {
        val changes = mutableListOf<AttributeChange<*>>()
        fun <T> compare(attr: String, a: T?, b: T?) {
            if (a != b) changes += AttributeChange(attr, a, b)
        }

        compare("name", name, other.name)

        return changes
    }
}
