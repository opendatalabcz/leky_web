package cz.machovec.lekovyportal.api.dto

import cz.machovec.lekovyportal.api.enum.CalculationMode
import cz.machovec.lekovyportal.api.enum.EReceptDataTypeAggregation
import cz.machovec.lekovyportal.api.enum.NormalisationMode

data class PrescriptionDispenseByDistrictTimeSeriesRequest(
    val dateFrom: String,
    val dateTo: String,
    val calculationMode: CalculationMode,
    val aggregationType: EReceptDataTypeAggregation,
    val normalisationMode: NormalisationMode,
    val medicinalProductIds: List<Long>
)

