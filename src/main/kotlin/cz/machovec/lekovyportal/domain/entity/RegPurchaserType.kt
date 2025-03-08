package cz.machovec.lekovyportal.domain.entity

enum class RegPurchaserType {
    DISTRIBUTOR, OOV;

    companion object {
        fun fromString(value: String): RegPurchaserType? {
            return entries.firstOrNull { it.name.equals(value.trim().uppercase(), ignoreCase = true) }
        }
    }
}
