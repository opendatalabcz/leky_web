package cz.machovec.lekovyportal.api.model

data class SankeyNodeDto(
    val id: String,
    val label: String
)

data class SankeyLinkDto(
    val source: String,
    val target: String,
    val value: Int
)

data class DistributionSankeyResponse(
    val nodes: List<SankeyNodeDto>,
    val links: List<SankeyLinkDto>,
    val includedMedicineProducts: List<MedicineProductInfo>,
    val ignoredMedicineProducts: List<MedicineProductInfo>
)
