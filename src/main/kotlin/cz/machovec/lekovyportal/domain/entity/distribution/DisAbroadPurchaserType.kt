package cz.machovec.lekovyportal.domain.entity.distribution

enum class DisAbroadPurchaserType {
    DISTRIBUTOR_EU,
    DISTRIBUTOR_MIMO_EU,
    OOV;

    companion object {
        fun fromString(value: String): DisAbroadPurchaserType? {
            return entries.firstOrNull { it.name.equals(value.trim().uppercase(), ignoreCase = true) }
        }
    }
}