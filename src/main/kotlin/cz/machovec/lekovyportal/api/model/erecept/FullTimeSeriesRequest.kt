package cz.machovec.lekovyportal.api.model.erecept

import cz.machovec.lekovyportal.api.model.enums.CalculationMode
import cz.machovec.lekovyportal.api.model.enums.EReceptDataTypeAggregation
import cz.machovec.lekovyportal.api.model.enums.NormalisationMode

data class FullTimeSeriesRequest(
    val medicinalProductIds: List<Long>,
    val registrationNumbers: List<String> = emptyList(),
    val aggregationType: EReceptDataTypeAggregation,
    val calculationMode: CalculationMode,
    val normalisationMode: NormalisationMode,
    val granularity: Granularity,
    val district: String? = null // the whole Czech Republic if not specified
)
