package cz.machovec.lekovyportal.core.dto.distribution

import cz.machovec.lekovyportal.core.domain.distribution.MahPurchaserType
import cz.machovec.lekovyportal.core.domain.distribution.MovementType

data class AggregateMahProductMovementCountDto(
    val medicinalProductId: Long,
    val purchaserType: MahPurchaserType,
    val movementType: MovementType,
    val packageCount: Long
)