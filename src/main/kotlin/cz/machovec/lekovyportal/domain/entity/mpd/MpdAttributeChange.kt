package cz.machovec.lekovyportal.domain.entity.mpd

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "mpd_attribute_change")
data class MpdAttributeChange(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "dataset_type", nullable = false)
    val datasetType: MpdDatasetType,

    @Column(name = "record_id", nullable = false)
    val recordId: Long,

    @Column(name = "attribute", nullable = false)
    val attribute: String,

    @Column(name = "old_value")
    val oldValue: String?,

    @Column(name = "new_value")
    val newValue: String?,

    @Column(name = "seen_in_dataset_valid_from", nullable = false)
    val seenInDatasetValidFrom: LocalDate
)
