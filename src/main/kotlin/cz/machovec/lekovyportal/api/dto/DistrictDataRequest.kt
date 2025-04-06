package cz.machovec.lekovyportal.api.dto

import cz.machovec.lekovyportal.api.enum.EReceptFilterType

data class DistrictDataRequest(
    val medicinalProductIds: List<Long>,
    val filterType: EReceptFilterType,
    val dateFrom: String?,
    val dateTo: String?
)
