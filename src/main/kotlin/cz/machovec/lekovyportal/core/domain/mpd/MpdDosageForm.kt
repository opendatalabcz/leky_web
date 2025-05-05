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
@Table(name = "mpd_dosage_form")
data class MpdDosageForm(
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
    val name: String?,

    @Column(name = "name_en")
    val nameEn: String?,

    @Column(name = "name_lat")
    val nameLat: String?,

    @Column(name = "is_cannabis")
    val isCannabis: Boolean?,

    @Column(name = "edqm_code")
    val edqmCode: Long?
) : BaseMpdEntity<MpdDosageForm>() {

    override fun getUniqueKey(): String {
        return code
    }

    override fun copyPreservingIdAndFirstSeen(from: MpdDosageForm): MpdDosageForm {
        return this.copy(
            id = from.id,
            firstSeen = from.firstSeen,
            missingSince = null
        )
    }

    override fun markMissing(since: LocalDate): MpdDosageForm {
        return this.copy(missingSince = since)
    }

    override fun getBusinessAttributeChanges(other: MpdDosageForm): List<AttributeChange<*>> {
        val changes = mutableListOf<AttributeChange<*>>()
        fun <T> compare(attr: String, a: T?, b: T?) {
            if (a != b) changes += AttributeChange(attr, a, b)
        }
        compare("name", name, other.name)
        compare("nameEn", nameEn, other.nameEn)
        compare("nameLat", nameLat, other.nameLat)
        compare("isCannabis", isCannabis, other.isCannabis)
        compare("edqmCode", edqmCode, other.edqmCode)
        return changes
    }
}
