package cz.machovec.lekovyportal.domain.entity

enum class DisPurchaserType {
    DISTRIBUTOR_CR,
    DISTRIBUTOR_EU,
    DISTRIBUTOR_MIMO_EU,
    LEKAR,
    LEKARNA,
    NUKLEARNI_MEDICINA,
    OBCHODNI_ZASTUPCE,
    OSOBA_POSKYTUJICI_ZDRAVOTNI_PECI,
    PRODEJCE_VLP,
    TRANSFUZNI_SLUZBA,
    VETERINARNI_LEKAR,
    ZAHRANICI;

    companion object {
        fun fromString(value: String): DisPurchaserType? {
            return entries.firstOrNull { it.name.equals(value.trim().uppercase(), ignoreCase = true) }
        }
    }
}
