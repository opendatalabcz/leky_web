package cz.machovec.lekovyportal.domain.entity

enum class RegPurchaserType {
    DISTRIBUTOR, OOV;

    companion object {
        fun fromString(value: String): RegPurchaserType? {
            return when (value.trim().uppercase()) {
                "DISTRIBUTOR" -> DISTRIBUTOR
                "OOV" -> OOV
                else -> null
            }
        }
    }
}