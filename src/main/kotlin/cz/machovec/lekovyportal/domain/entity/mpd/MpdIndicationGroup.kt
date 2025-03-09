package cz.machovec.lekovyportal.domain.entity.mpd

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "mpd_indication_group")
data class MpdIndicationGroup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "code", nullable = false, unique = true)
    val code: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "valid_from", nullable = false)
    val validFrom: LocalDate,

    @Column(name = "valid_to")
    val validTo: LocalDate? = null
)
