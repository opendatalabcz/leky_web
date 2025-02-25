package cz.machovec.lekovyportal.api.dto

data class EReceptDataResponse(
    val districtName: String,
    val prescribed: Int,
    val dispensed: Int,
    val difference: Int,
)
