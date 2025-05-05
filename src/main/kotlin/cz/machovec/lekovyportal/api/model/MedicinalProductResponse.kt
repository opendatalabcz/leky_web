package cz.machovec.lekovyportal.api.model

data class MedicinalProductResponse(
    val id: Long,
    val name: String,
    val supplementaryInformation: String?,
    val suklCode: String,
    val registrationNumber: String?,
    val atcGroup: AtcGroupResponse?
)
