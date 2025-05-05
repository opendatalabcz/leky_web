package cz.machovec.lekovyportal.core.domain.distribution

enum class DistributorExportPurchaserType(
    val csvValue: String,
    val descriptionCs: String
) {
    DISTRIBUTOR_EU("DISTRIBUTOR_EU", "Sklad distributora v EU (mimo území ČR)"),
    DISTRIBUTOR_NON_EU("DISTRIBUTOR_MIMO_EU", "Sklad distributora mimo území EU"),
    AUTHORIZED_PERSON("OOV", "Osoba oprávněná k výdeji v zahraničí");

    companion object {
        fun fromInput(value: String): DistributorExportPurchaserType? {
            return entries.firstOrNull {
                it.csvValue == value.trim()
            }
        }
    }
}
