package cz.machovec.lekovyportal.core.dto.distribution

import java.math.BigDecimal

data class MonthlyPharmacyAggregate(
    val year: Int,
    val month: Int,
    val packageCount: BigDecimal
)