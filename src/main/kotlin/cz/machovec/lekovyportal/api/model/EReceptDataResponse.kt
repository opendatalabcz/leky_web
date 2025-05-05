package cz.machovec.lekovyportal.api.model

data class EReceptDataResponse(
    val districtName: String,
    val prescribed: Int,
    val dispensed: Int,
    val difference: Int,
)
