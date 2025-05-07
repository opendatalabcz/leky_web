package cz.machovec.lekovyportal.api.model.mpd

data class MpdMedicinalProductGroupedByRegNumberDto(
    val registrationNumber: String,
    val suklCodes: List<String>,
    val names: List<String>,
    val strengths: List<String>,
    val dosageFormIds: List<Long>,
    val administrationRouteIds: List<Long>,
    val atcGroupIds: List<Long>,
    val substanceIds: List<Long>
)
