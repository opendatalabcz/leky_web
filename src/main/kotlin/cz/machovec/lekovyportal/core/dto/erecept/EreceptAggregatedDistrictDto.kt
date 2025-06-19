package cz.machovec.lekovyportal.core.dto.erecept

import java.math.BigDecimal

data class EreceptAggregatedDistrictDto(
    val districtCode: String,
    val medicinalProductId: Long,
    val prescribed: BigDecimal,
    val dispensed: BigDecimal,
    val population: Int
)