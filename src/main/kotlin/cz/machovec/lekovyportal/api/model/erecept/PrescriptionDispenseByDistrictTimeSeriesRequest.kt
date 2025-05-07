package cz.machovec.lekovyportal.api.model.erecept

import cz.machovec.lekovyportal.api.model.enums.CalculationMode
import cz.machovec.lekovyportal.api.model.enums.EReceptDataTypeAggregation
import cz.machovec.lekovyportal.api.model.enums.NormalisationMode

data class PrescriptionDispenseByDistrictTimeSeriesRequest(
    val dateFrom: String,
    val dateTo: String,
    val calculationMode: CalculationMode,
    val aggregationType: EReceptDataTypeAggregation,
    val normalisationMode: NormalisationMode,
    val medicinalProductIds: List<Long>,
    val registrationNumbers: List<String>
)

