package cz.machovec.lekovyportal.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "dis_abroad_distribution")
data class DisAbroadDistribution(
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
    val purchaserType: DisAbroadPurchaserType,

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    val movementType: MovementType,

    @Column(name = "package_count", nullable = false)
    val packageCount: Int,

    @Column(name = "subject", nullable = false)
    val subject: String, // TODO: Find dataset to connect via FK. Maybe dlp_organisation?
)