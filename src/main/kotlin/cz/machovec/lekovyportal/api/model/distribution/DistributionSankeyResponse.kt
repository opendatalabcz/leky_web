package cz.machovec.lekovyportal.api.model.distribution

import cz.machovec.lekovyportal.api.model.mpd.MedicinalProductIdentificators

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
    val includedMedicineProducts: List<MedicinalProductIdentificators>,
    val ignoredMedicineProducts: List<MedicinalProductIdentificators>
)
