package cz.machovec.lekovyportal.api.dto

import cz.machovec.lekovyportal.api.enum.CalculationMode
import cz.machovec.lekovyportal.api.enum.EReceptDataTypeAggregation
import cz.machovec.lekovyportal.api.enum.NormalisationMode

data class FullTimeSeriesRequest(
    val medicinalProductIds: List<Long>,
    val aggregationType: EReceptDataTypeAggregation,
    val calculationMode: CalculationMode,
    val normalisationMode: NormalisationMode,
    val granularity: Granularity,
    val district: String? = null // pokud není zadán, tak celá ČR
)
