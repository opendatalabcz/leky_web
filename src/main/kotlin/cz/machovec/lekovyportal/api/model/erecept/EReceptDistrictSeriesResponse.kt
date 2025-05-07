package cz.machovec.lekovyportal.api.model.erecept

import cz.machovec.lekovyportal.api.model.mpd.MedicinalProductIdentificators
import cz.machovec.lekovyportal.api.model.enums.CalculationMode
import cz.machovec.lekovyportal.api.model.enums.EReceptDataTypeAggregation
import cz.machovec.lekovyportal.api.model.enums.NormalisationMode

data class PrescriptionDispenseByDistrictTimeSeriesResponse(
    val aggregationType: EReceptDataTypeAggregation,
    val calculationMode: CalculationMode,
    val normalisationMode: NormalisationMode,
    val dateFrom: String,
    val dateTo: String,
    val series: List<TimeSeriesMonthDistrictValues>,
    val includedMedicineProducts: List<MedicinalProductIdentificators>,
    val ignoredMedicineProducts: List<MedicinalProductIdentificators>
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