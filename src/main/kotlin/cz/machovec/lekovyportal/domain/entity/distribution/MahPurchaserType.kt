package cz.machovec.lekovyportal.domain.entity.distribution

enum class MahPurchaserType(
    val csvValue: String,
    val descriptionCs: String
) {
    DISTRIBUTOR("DISTRIBUTOR", "Distributor na území ČR"),
    AUTHORIZED_PERSON("OOV", "Osoba oprávněná k výdeji v ČR");

    companion object {
        fun fromInput(value: String): MahPurchaserType? {
            return entries.firstOrNull {
                it.csvValue == value.trim()
            }
        }
    }
}
