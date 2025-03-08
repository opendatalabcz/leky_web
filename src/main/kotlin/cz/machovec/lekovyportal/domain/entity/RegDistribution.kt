package cz.machovec.lekovyportal.domain.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "reg_distribution")
data class RegDistribution(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "year", nullable = false)
    val year: Int,

    @Column(name = "month", nullable = false)
    val month: Int,

    @Column(name = "sukl_code", nullable = false)
    val suklCode: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "purchaser_type", nullable = false)
    val purchaserType: RegPurchaserType,

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    val movementType: MovementType,

    @Column(name = "package_count", nullable = false)
    val packageCount: Int
)
