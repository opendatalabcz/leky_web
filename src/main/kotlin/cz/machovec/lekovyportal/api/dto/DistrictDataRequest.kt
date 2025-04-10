package cz.machovec.lekovyportal.api.dto

import cz.machovec.lekovyportal.api.enum.CalculationMode
import cz.machovec.lekovyportal.api.enum.EReceptDataTypeAggregation
import cz.machovec.lekovyportal.api.enum.NormalisationMode

data class DistrictDataRequest(
    val medicinalProductIds: List<Long>,
    val aggregationType: EReceptDataTypeAggregation,
    val dateFrom: String?,
    val dateTo: String?,
    val calculationMode: CalculationMode,
    val normalisationMode: NormalisationMode
)
