package cz.machovec.lekovyportal.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
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

    @Column(name = "sukl_code", nullable = false)
    val suklCode: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "dispense_type", nullable = false)
    val dispenseType: LekDispenseType,

    @Column(name = "package_count", nullable = false)
    val packageCount: BigDecimal
)