package cz.machovec.lekovyportal.domain.entity.distribution

enum class DistributorPurchaserType(
    val csvValue: String,
    val descriptionCs: String
) {
    DISTRIBUTOR_CR("DISTRIBUTOR_CR", "Sklad distributora na území ČR"),
    DISTRIBUTOR_EU("DISTRIBUTOR_EU", "Sklad distributora v EU (mimo území ČR)"),
    DISTRIBUTOR_NON_EU("DISTRIBUTOR_MIMO_EU", "Sklad distributora mimo území EU"),
    DOCTOR("LEKAR", "Lékař (pouze imunologické přípravky za účelem očkování)"),
    PHARMACY("LEKARNA", "Lékárna"),
    NUCLEAR_MEDICINE("NUKLEARNI_MEDICINA", "Pracoviště nukleární medicíny (pouze radiofarmaka)"),
    SALES_REPRESENTATIVE("OBCHODNI_ZASTUPCE", "Držitelé rozhodnutí o registraci nebo obchodní zástupci (reklamní vzorky)"),
    HEALTHCARE_PROVIDER("OSOBA_POSKYTUJICI_ZDRAVOTNI_PECI", "Osoba poskytující zdravotní péči (plyny při poskytování péče, infuzní a dialyzační roztoky)"),
    VLP_SELLER("PRODEJCE_VLP", "Prodejce vyhrazených léčiv (pouze vyhrazené LP)"),
    TRANSFUSION_SERVICE("TRANSFUZNI_SLUZBA", "Zařízení transfúzní služby (pouze krevní deriváty)"),
    VETERINARY_DOCTOR("VETERINARNI_LEKAR", "Veterinární lékař"),
    FOREIGN_ENTITY("ZAHRANICI", "Osoba oprávněná k výdeji v zahraničí");

    companion object {
        fun fromInput(value: String): DistributorPurchaserType? {
            return entries.firstOrNull {
                it.csvValue == value.trim()
            }
        }
    }
}
