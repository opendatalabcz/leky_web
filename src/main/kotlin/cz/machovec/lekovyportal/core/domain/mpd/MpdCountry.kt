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
@Table(name = "mpd_country")
data class MpdCountry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override val id: Long? = null,

    @Column(name = "first_seen", nullable = false)
    override val firstSeen: LocalDate,

    @Column(name = "missing_since")
    override val missingSince: LocalDate?,

    @Column(name = "code", nullable = false, unique = true, length = 3)
    val code: String,

    @Column(name = "name")
    val name: String?,

    @Column(name = "name_en")
    val nameEn: String?,

    @Column(name = "edqm_code", length = 2)
    val edqmCode: String?
) : BaseMpdEntity<MpdCountry>() {

    override fun getUniqueKey(): String = code

    override fun copyPreservingIdAndFirstSeen(from: MpdCountry): MpdCountry {
        return this.copy(
            id = from.id,
            firstSeen = from.firstSeen,
            missingSince = null
        )
    }

    override fun markMissing(since: LocalDate): MpdCountry {
        return this.copy(missingSince = since)
    }

    override fun getBusinessAttributeChanges(other: MpdCountry): List<AttributeChange<*>> {
        val changes = mutableListOf<AttributeChange<*>>()
        fun <T> compare(attr: String, a: T?, b: T?) {
            if (a != b) changes += AttributeChange(attr, a, b)
        }

        compare("name", name, other.name)
        compare("nameEn", nameEn, other.nameEn)
        compare("edqmCode", edqmCode, other.edqmCode)

        return changes
    }
}
