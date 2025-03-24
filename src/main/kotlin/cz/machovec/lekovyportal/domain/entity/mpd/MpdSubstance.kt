package cz.machovec.lekovyportal.domain.entity.mpd

import cz.machovec.lekovyportal.domain.AttributeChange
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "mpd_substance")
data class MpdSubstance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override val id: Long? = null,

    @Column(name = "first_seen", nullable = false)
    override val firstSeen: LocalDate,

    @Column(name = "missing_since")
    override val missingSince: LocalDate?,

    @Column(name = "code", nullable = false, unique = true)
    val code: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    val source: MpdSource?,

    @Column(name = "name_inn")
    val nameInn: String?,

    @Column(name = "name_en")
    val nameEn: String?,

    @Column(name = "name")
    val name: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addiction_category_id")
    val addictionCategory: MpdAddictionCategory?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doping_category_id")
    val dopingCategory: MpdDopingCategory?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "government_regulation_category_id")
    val governmentRegulationCategory: MpdGovernmentRegulationCategory?

) : BaseMpdEntity<MpdSubstance>() {

    override fun getUniqueKey(): String = code

    override fun copyPreservingIdAndFirstSeen(from: MpdSubstance): MpdSubstance {
        return this.copy(
            id = from.id,
            firstSeen = from.firstSeen,
            missingSince = null
        )
    }

    override fun markMissing(since: LocalDate): MpdSubstance {
        return this.copy(missingSince = since)
    }

    override fun getBusinessAttributeChanges(other: MpdSubstance): List<AttributeChange<*>> {
        val changes = mutableListOf<AttributeChange<*>>()
        fun <T> compare(attr: String, a: T?, b: T?) {
            if (a != b) changes += AttributeChange(attr, a, b)
        }

        compare("nameInn", nameInn, other.nameInn)
        compare("nameEn", nameEn, other.nameEn)
        compare("name", name, other.name)
        compare("source", source?.id, other.source?.id)
        compare("addictionCategory", addictionCategory?.id, other.addictionCategory?.id)
        compare("dopingCategory", dopingCategory?.id, other.dopingCategory?.id)
        compare("governmentRegulationCategory", governmentRegulationCategory?.id, other.governmentRegulationCategory?.id)

        return changes
    }
}
