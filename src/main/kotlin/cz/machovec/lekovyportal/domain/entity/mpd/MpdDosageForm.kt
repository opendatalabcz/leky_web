package cz.machovec.lekovyportal.domain.entity.mpd

import cz.machovec.lekovyportal.domain.AttributeChange
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
    val id: Long? = null,

    @Column(name = "code", nullable = false, unique = true)
    val code: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "name_en", nullable = false)
    val nameEn: String,

    @Column(name = "name_lat", nullable = false)
    val nameLat: String,

    @Column(name = "is_cannabis", nullable = false)
    val isCannabis: Boolean,

    @Column(name = "edqm_code")
    val edqmCode: Long?,

    @Column(name = "first_seen", nullable = false)
    val firstSeen: LocalDate,

    @Column(name = "missing_since")
    val missingSince: LocalDate?,
) {
    fun getBusinessAttributeChanges(other: MpdDosageForm): List<AttributeChange<*>> {
        val changes = mutableListOf<AttributeChange<*>>()

        if (this.name != other.name)
            changes += AttributeChange("name", this.name, other.name)
        if (this.nameEn != other.nameEn)
            changes += AttributeChange("nameEn", this.nameEn, other.nameEn)
        if (this.nameLat != other.nameLat)
            changes += AttributeChange("nameLat", this.nameLat, other.nameLat)
        if (this.isCannabis != other.isCannabis)
            changes += AttributeChange("isCannabis", this.isCannabis, other.isCannabis)
        if (this.edqmCode != other.edqmCode)
            changes += AttributeChange("edqmCode", this.edqmCode, other.edqmCode)

        return changes
    }
}
