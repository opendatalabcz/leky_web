package cz.machovec.lekovyportal.core.dto.distribution

import cz.machovec.lekovyportal.core.domain.distribution.DistributorPurchaserType
import cz.machovec.lekovyportal.core.domain.distribution.MovementType

data class AggregateDistributorProductMovementCountDto(
    val medicinalProductId: Long,
    val purchaserType: DistributorPurchaserType,
    val movementType: MovementType,
    val packageCount: Long
)