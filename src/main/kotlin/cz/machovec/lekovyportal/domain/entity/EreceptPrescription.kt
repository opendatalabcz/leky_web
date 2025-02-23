package cz.machovec.lekovyportal.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table

@Entity
@Table(name = "erecept_prescription")
@IdClass(EreceptPrescriptionId::class)
data class EreceptPrescription(
    @Id
    @Column(name = "district_code", nullable = false)
    val districtCode: String,

    @Id
    @Column(name = "year", nullable = false)
    val year: Int,

    @Id
    @Column(name = "month", nullable = false)
    val month: Int,

    @Id
    @Column(name = "sukl_code", nullable = false)
    val suklCode: String,

    @Column(name = "quantity", nullable = false)
    val quantity: Int
)

