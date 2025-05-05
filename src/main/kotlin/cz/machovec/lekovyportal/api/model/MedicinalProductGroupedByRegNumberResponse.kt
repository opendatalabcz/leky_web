package cz.machovec.lekovyportal.api.model

data class MedicinalProductGroupedByRegNumberResponse(
    val registrationNumber: String,
    val suklCodes: List<String>,
    val names: List<String>,
    val strengths: List<String>,
    val dosageForms: List<DosageFormResponse>,
    val administrationRoutes: List<AdministrationRouteResponse>,
    val atcGroups: List<AtcGroupResponse>
)