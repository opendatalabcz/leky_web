package cz.machovec.lekovyportal.core.dto.erecept

import java.math.BigDecimal

data class EreceptTimeSeriesDistrictDto(
    val year: Int,
    val month: Int,
    val districtCode: String,
    val medicinalProductId: Long,
    val prescribed: BigDecimal,
    val dispensed: BigDecimal,
    val population: Int
)