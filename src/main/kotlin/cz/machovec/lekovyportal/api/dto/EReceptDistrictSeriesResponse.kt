package cz.machovec.lekovyportal.api.dto

import cz.machovec.lekovyportal.api.enum.CalculationMode
import cz.machovec.lekovyportal.api.enum.EReceptDataTypeAggregation
import cz.machovec.lekovyportal.api.enum.NormalisationMode

data class PrescriptionDispenseByDistrictTimeSeriesResponse(
    val aggregationType: EReceptDataTypeAggregation,
    val calculationMode: CalculationMode,
    val normalisationMode: NormalisationMode,
    val dateFrom: String,
    val dateTo: String,
    val series: List<TimeSeriesMonthDistrictValues>,
    val includedMedicineProducts: List<MedicineProductInfo>,
    val ignoredMedicineProducts: List<MedicineProductInfo>
)

data class TimeSeriesMonthDistrictValues(
    val month: String,
    val values: Map<String, Int>,
    val summary: SummaryValues
)

data class SummaryValues(
    val prescribed: Int,
    val dispensed: Int,
    val difference: Int,
    val percentageDifference: Double
)