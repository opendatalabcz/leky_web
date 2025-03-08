package cz.machovec.lekovyportal.domain.entity

enum class LekDispenseType {
    PRESCRIPTION,
    REQUISITION,
    OTC;

    companion object {
        fun fromString(value: String): LekDispenseType? {
            return when (value.trim().lowercase()) {
                "recept" -> PRESCRIPTION
                "žádanka" -> REQUISITION
                "volný" -> OTC
                else -> null
            }
        }
    }
}
