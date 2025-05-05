package cz.machovec.lekovyportal.core.domain.mpd

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "mpd_record_temporary_absence")
data class MpdRecordTemporaryAbsence(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "dataset_type", nullable = false)
    val datasetType: MpdDatasetType, // TODO rename to mpdDatasetType

    @Column(name = "record_id", nullable = false)
    val recordId: Long,

    @Column(name = "missing_from", nullable = false)
    val missingFrom: LocalDate,

    @Column(name = "missing_to", nullable = false)
    val missingTo: LocalDate
)
