package cz.machovec.lekovyportal.core.dto.erecept

data class EreceptTimeSeriesDistrictDto(
    val year: Int,
    val month: Int,
    val districtCode: String,
    val medicinalProductId: Long,
    val prescribed: Int,
    val dispensed: Int,
    val population: Int
)