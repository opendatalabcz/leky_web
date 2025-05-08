package cz.machovec.lekovyportal.core.dto.distribution

import cz.machovec.lekovyportal.core.domain.distribution.PharmacyDispenseType
import java.math.BigDecimal

data class MonthlyPharmacyProductDispenseCountDto(
    val year: Int,
    val month: Int,
    val medicinalProductId: Long,
    val dispenseType: PharmacyDispenseType,
    val packageCount: BigDecimal
)