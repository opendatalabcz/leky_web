package cz.machovec.lekovyportal.domain.entity.distribution

import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProduct
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
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "lek_distribution")
data class LekDistribution(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "year", nullable = false)
    val year: Int,

    @Column(name = "month", nullable = false)
    val month: Int,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicinal_product_id", nullable = false)
    val medicinalProduct: MpdMedicinalProduct,

    @Enumerated(EnumType.STRING)
    @Column(name = "dispense_type", nullable = false)
    val dispenseType: LekDispenseType,

    @Column(name = "package_count", nullable = false)
    val packageCount: BigDecimal
)