package cz.machovec.lekovyportal.domain.dto

import java.math.BigDecimal

data class MonthlyPharmacyAggregate(
    val year: Int,
    val month: Int,
    val packageCount: BigDecimal
)