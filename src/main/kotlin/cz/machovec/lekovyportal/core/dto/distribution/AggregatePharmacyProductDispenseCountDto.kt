package cz.machovec.lekovyportal.core.dto.distribution

import cz.machovec.lekovyportal.core.domain.distribution.PharmacyDispenseType
import java.math.BigDecimal

data class AggregatePharmacyProductDispenseCountDto(
    val medicinalProductId: Long,
    val dispenseType: PharmacyDispenseType,
    val packageCount: BigDecimal
)