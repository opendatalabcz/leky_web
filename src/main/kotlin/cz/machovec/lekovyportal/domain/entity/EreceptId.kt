package cz.machovec.lekovyportal.domain.entity

import java.io.Serializable

/**
 * Composite key for EreceptPrescription entity.
 */
data class EreceptPrescriptionId(
    val districtCode: String,
    val year: Int,
    val month: Int,
    val suklCode: String
) : Serializable

/**
 * Composite key for EreceptDispense entity.
 */
data class EreceptDispenseId(
    val districtCode: String,
    val year: Int,
    val month: Int,
    val suklCode: String
) : Serializable
