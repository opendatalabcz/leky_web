import cz.machovec.lekovyportal.api.model.enums.CalculationMode
import cz.machovec.lekovyportal.api.model.enums.EreceptType
import cz.machovec.lekovyportal.api.model.enums.TimeGranularity
import cz.machovec.lekovyportal.api.model.enums.NormalisationMode
import cz.machovec.lekovyportal.api.model.mpd.MedicinalProductIdentificators

// ==========================
// DTOs for /api/erecept/prescription-dispense/time-aggregate/by-district
// ==========================

data class EreceptAggregateByDistrictRequest(
    val medicinalProductIds: List<Long>,
    val registrationNumbers: List<String> = emptyList(),
    val dateFrom: String?,
    val dateTo: String?,
    val aggregationType: EreceptType,
    val calculationMode: CalculationMode,
    val normalisationMode: NormalisationMode
)

data class EreceptAggregateByDistrictResponse(
    val includedMedicineProducts: List<MedicinalProductIdentificators>,
    val ignoredMedicineProducts: List<MedicinalProductIdentificators>,
    val dateFrom: String?,
    val dateTo: String?,
    val aggregationType: EreceptType,
    val calculationMode: CalculationMode,
    val normalisationMode: NormalisationMode,
    val districtValues: Map<String, Int>,
    val summary: SummaryValues
)

// ==========================
// DTOs for /api/erecept/prescription-dispense/time-series/by-district
// ==========================

data class EreceptTimeSeriesByDistrictRequest(
    val medicinalProductIds: List<Long>,
    val registrationNumbers: List<String>,
    val dateFrom: String,
    val dateTo: String,
    val aggregationType: EreceptType,
    val calculationMode: CalculationMode,
    val normalisationMode: NormalisationMode
)

data class EreceptTimeSeriesByDistrictResponse(
    val includedMedicineProducts: List<MedicinalProductIdentificators>,
    val ignoredMedicineProducts: List<MedicinalProductIdentificators>,
    val dateFrom: String,
    val dateTo: String,
    val aggregationType: EreceptType,
    val calculationMode: CalculationMode,
    val normalisationMode: NormalisationMode,
    val series: List<TimeSeriesMonthDistrictValues>
)

data class TimeSeriesMonthDistrictValues(
    val month: String,
    val districtValues: Map<String, Int>,
    val summary: SummaryValues
)

data class SummaryValues(
    val prescribed: Int,
    val dispensed: Int,
    val difference: Int,
    val percentageDifference: Double
)

// ==========================
// DTOs for /api/erecept/prescription-dispense/time-series
// ==========================

data class EreceptFullTimeSeriesRequest(
    val medicinalProductIds: List<Long>,
    val registrationNumbers: List<String> = emptyList(),
    val calculationMode: CalculationMode,
    val normalisationMode: NormalisationMode,
    val timeGranularity: TimeGranularity,
    val district: String? = null // null = whole Czech Republic
)

data class EreceptFullTimeSeriesResponse(
    val includedMedicineProducts: List<MedicinalProductIdentificators>,
    val ignoredMedicineProducts: List<MedicinalProductIdentificators>,
    val calculationMode: CalculationMode,
    val normalisationMode: NormalisationMode,
    val timeGranularity: TimeGranularity,
    val district: String?, // null = whole Czech Republic
    val series: List<EreceptFullTimeSeriesEntry>
)

data class EreceptFullTimeSeriesEntry(
    val period: String, // e.g. "2023-05" or "2023"
    val prescribed: Int,
    val dispensed: Int,
    val difference: Int
)
