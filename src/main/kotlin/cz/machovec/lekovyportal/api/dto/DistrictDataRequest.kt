package cz.machovec.lekovyportal.api.dto

import cz.machovec.lekovyportal.api.enum.CalculationMode
import cz.machovec.lekovyportal.api.enum.EReceptFilterType
import cz.machovec.lekovyportal.api.enum.NormalisationMode

data class DistrictDataRequest(
    val medicinalProductIds: List<Long>,
    val filterType: EReceptFilterType,
    val dateFrom: String?,
    val dateTo: String?,
    val calculationMode: CalculationMode,
    val normalisationMode: NormalisationMode
)
