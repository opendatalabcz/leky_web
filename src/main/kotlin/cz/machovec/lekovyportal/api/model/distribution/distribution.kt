import cz.machovec.lekovyportal.api.model.enums.MedicinalUnitMode
import cz.machovec.lekovyportal.api.model.enums.TimeGranularity
import cz.machovec.lekovyportal.api.model.mpd.MedicinalProductIdentificators

// ==========================
// DTOs for /api/distribution/sankey-diagram
// ==========================

data class DistributionSankeyRequest(
    val medicinalProductIds: List<Long>,
    val registrationNumbers: List<String>,
    val dateFrom: String,
    val dateTo: String,
    val medicinalUnitMode: MedicinalUnitMode
)

data class DistributionSankeyResponse(
    val includedMedicineProducts: List<MedicinalProductIdentificators>,
    val ignoredMedicineProducts: List<MedicinalProductIdentificators>,
    val dateFrom: String,
    val dateTo: String,
    val medicinalUnitMode: MedicinalUnitMode,
    val nodes: List<SankeyNodeDto>,
    val links: List<SankeyLinkDto>
)

data class SankeyNodeDto(
    val id: String,
    val label: String
)

data class SankeyLinkDto(
    val source: String,
    val target: String,
    val value: Int
)

// ==========================
// DTOs for /api/distribution/time-series
// ==========================

data class DistributionTimeSeriesRequest(
    val medicinalProductIds: List<Long>,
    val registrationNumbers: List<String>,
    val dateFrom: String, // "yyyy-MM"
    val dateTo: String,   // "yyyy-MM"
    val medicinalUnitMode: MedicinalUnitMode,
    val timeGranularity: TimeGranularity
)

data class DistributionTimeSeriesResponse(
    val includedMedicineProducts: List<MedicinalProductIdentificators>,
    val ignoredMedicineProducts: List<MedicinalProductIdentificators>,
    val dateFrom: String,
    val dateTo: String,
    val medicinalUnitMode: MedicinalUnitMode,
    val timeGranularity: TimeGranularity,
    val series: List<DistributionTimeSeriesPeriodEntry>
)

data class DistributionTimeSeriesPeriodEntry(
    val period: String, // e.g. "2023-05" or "2023"
    val flows: List<DistributionFlowEntry>
)

data class DistributionFlowEntry(
    val source: String,
    val target: String,
    val value: Int
)
