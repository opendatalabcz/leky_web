package cz.machovec.lekovyportal.core.dto.erecept

import java.math.BigDecimal

data class EreceptFullTimeSeriesDto(
    val year: Int,
    val month: Int,
    val districtCode: String,
    val medicinalProductId: Long,
    val prescribed: BigDecimal,
    val dispensed: BigDecimal
)