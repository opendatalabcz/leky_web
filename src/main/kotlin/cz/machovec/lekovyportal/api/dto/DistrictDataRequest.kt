package cz.machovec.lekovyportal.api.dto

data class DistrictDataRequest(
    val medicinalProductIds: List<Long>,
    val filterType: String, // "prescribed", "dispensed", "difference"
)
