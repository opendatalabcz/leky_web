package cz.machovec.lekovyportal.domain.entity.distribution

enum class MovementType(
    val csvValue: String,
    val descriptionCs: String
) {
    DELIVERY("D", "Dodávka LP"),
    RETURN("V", "Vratka LP");

    companion object {
        fun fromInput(value: String): MovementType? {
            return entries.firstOrNull {
                it.csvValue == value.trim().uppercase()
            }
        }
    }
}
