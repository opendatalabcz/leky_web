package cz.machovec.lekovyportal.core.dto.distribution

import cz.machovec.lekovyportal.core.domain.distribution.MovementType

data class MonthlyMovementAggregate(
    val year: Int,
    val month: Int,
    val purchaserType: Enum<*>, // MahPurchaserType or DistributorPurchaserType
    val movementType: MovementType,
    val totalCount: Long
)
