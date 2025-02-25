package cz.machovec.lekovyportal.domain.entity

enum class MovementType {
    DELIVERY, RETURN;

    companion object {
        fun fromString(value: String): MovementType? {
            return when (value.trim().uppercase()) {
                "D" -> DELIVERY
                "V" -> RETURN
                else -> null
            }
        }
    }
}