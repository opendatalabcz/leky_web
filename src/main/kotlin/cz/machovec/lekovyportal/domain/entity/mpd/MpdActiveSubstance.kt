package cz.machovec.lekovyportal.domain.entity.mpd

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
@Table(name = "mpd_active_substance")
data class MpdActiveSubstance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "code", nullable = false, unique = true)
    val code: String,

    @Column(name = "name_inn", nullable = false)
    val nameInn: String,

    @Column(name = "name_en", nullable = false)
    val nameEn: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addiction_category_id", nullable = true)
    val addictionCategory: MpdAddictionCategory?,

    @Column(name = "valid_from", nullable = false)
    val validFrom: LocalDate,

    @Column(name = "valid_to")
    val validTo: LocalDate? = null
)
