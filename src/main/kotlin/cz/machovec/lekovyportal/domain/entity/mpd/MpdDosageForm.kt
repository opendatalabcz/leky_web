package cz.machovec.lekovyportal.domain.entity.mpd

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
    val edqmCode: Long? = null,

    @Column(name = "valid_from", nullable = false)
    val validFrom: LocalDate,

    @Column(name = "valid_to")
    val validTo: LocalDate? = null
)
