package cz.machovec.lekovyportal.core.dto.erecept

data class EreceptAggregatedDistrictDto(
    val districtCode: String,
    val medicinalProductId: Long,
    val prescribed: Int,
    val dispensed: Int,
    val population: Int
)