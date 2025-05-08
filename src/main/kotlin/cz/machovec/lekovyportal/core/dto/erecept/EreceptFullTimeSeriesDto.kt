package cz.machovec.lekovyportal.core.dto.erecept

data class EreceptFullTimeSeriesDto(
    val year: Int,
    val month: Int,
    val districtCode: String,
    val medicinalProductId: Long,
    val prescribed: Int,
    val dispensed: Int
)