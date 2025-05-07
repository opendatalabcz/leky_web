import cz.machovec.lekovyportal.api.model.enums.CalculationMode
import cz.machovec.lekovyportal.api.model.erecept.Granularity
import cz.machovec.lekovyportal.api.model.mpd.MedicinalProductIdentificators

// ==========================
// DTOs for /api/distribution/sankey-diagram
// ==========================

data class DistributionSankeyRequest(
    val medicinalProductIds: List<Long>,
    val registrationNumbers: List<String>,
    val dateFrom: String,
    val dateTo: String,
    val calculationMode: CalculationMode
)

data class DistributionSankeyResponse(
    val includedMedicineProducts: List<MedicinalProductIdentificators>,
    val ignoredMedicineProducts: List<MedicinalProductIdentificators>,
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
    val calculationMode: CalculationMode,
    val granularity: Granularity
)

data class DistributionTimeSeriesResponse(
    val includedMedicineProducts: List<MedicinalProductIdentificators>,
    val ignoredMedicineProducts: List<MedicinalProductIdentificators>,
    val granularity: Granularity,
    val series: List<DistributionTimeSeriesEntry>
)

data class DistributionTimeSeriesEntry(
    val period: String, // e.g. "2023-05" or "2023"
    val mahToDistributor: Int,
    val distributorToPharmacy: Int,
    val pharmacyToPatient: Int
)