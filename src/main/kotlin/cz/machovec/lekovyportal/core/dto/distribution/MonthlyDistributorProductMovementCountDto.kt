package cz.machovec.lekovyportal.core.dto.distribution

import cz.machovec.lekovyportal.core.domain.distribution.DistributorPurchaserType
import cz.machovec.lekovyportal.core.domain.distribution.MovementType
import java.math.BigDecimal

data class MonthlyDistributorProductMovementCountDto(
    val year: Int,
    val month: Int,
    val medicinalProductId: Long,
    val purchaserType: DistributorPurchaserType,
    val movementType: MovementType,
    val packageCount: BigDecimal
)