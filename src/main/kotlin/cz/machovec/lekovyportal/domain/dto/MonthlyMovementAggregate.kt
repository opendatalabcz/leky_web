package cz.machovec.lekovyportal.domain.dto

import cz.machovec.lekovyportal.domain.entity.distribution.MovementType

data class MonthlyMovementAggregate(
    val year: Int,
    val month: Int,
    val purchaserType: Enum<*>, // MahPurchaserType or DistributorPurchaserType
    val movementType: MovementType,
    val totalCount: Long
)
