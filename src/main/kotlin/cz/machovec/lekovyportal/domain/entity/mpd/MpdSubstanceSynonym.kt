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
@Table(name = "mpd_substance_synonym")
data class MpdSubstanceSynonym(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substance_id", nullable = false)
    val substance: MpdSubstance,

    @Column(name = "sequence_number", nullable = false)
    val sequenceNumber: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    val source: MpdSource,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "valid_from", nullable = false)
    val validFrom: LocalDate,

    @Column(name = "valid_to")
    val validTo: LocalDate? = null
)
