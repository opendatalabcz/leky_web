package cz.machovec.lekovyportal.api.model

import cz.machovec.lekovyportal.api.model.enums.CalculationMode
import cz.machovec.lekovyportal.api.model.enums.EReceptDataTypeAggregation
import cz.machovec.lekovyportal.api.model.enums.NormalisationMode

data class DistrictDataRequest(
    val medicinalProductIds: List<Long>,
    val aggregationType: EReceptDataTypeAggregation,
    val dateFrom: String?,
    val dateTo: String?,
    val calculationMode: CalculationMode,
    val normalisationMode: NormalisationMode
)
