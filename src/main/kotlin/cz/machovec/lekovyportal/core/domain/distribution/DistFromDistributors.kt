package cz.machovec.lekovyportal.core.domain.distribution

import cz.machovec.lekovyportal.core.domain.mpd.MpdMedicinalProduct
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "dist_from_distributors")
data class DistFromDistributors(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "year", nullable = false)
    val year: Int,

    @Column(name = "month", nullable = false)
    val month: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicinal_product_id", nullable = false)
    val medicinalProduct: MpdMedicinalProduct,

    @Enumerated(EnumType.STRING)
    @Column(name = "purchaser_type", nullable = false)
    val purchaserType: DistributorPurchaserType,

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    val movementType: MovementType,

    @Column(name = "package_count", nullable = false)
    val packageCount: Int
)
