package cz.machovec.lekovyportal.domain.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@NoArg
@Entity
@Table(name = "processed_dataset")
data class ProcessedDataset(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "dataset_type", nullable = false)
    val datasetType: DatasetType,

    @Column(name = "year", nullable = false)
    val year: Int,

    @Column(name = "month", nullable = false)
    val month: Int
)
