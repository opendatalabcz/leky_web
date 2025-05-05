package cz.machovec.lekovyportal.core.domain.distribution

enum class PharmacyDispenseType(
    val csvValue: String,
    val descriptionCs: String
) {
    PRESCRIPTION("recept", "Lékařský předpis"),
    REQUISITION("žádanka", "Žádanka poskytovatelů zdravotních služeb"),
    OTC("volný", "Výdej bez lékařského předpisu s omezením");

    companion object {
        fun fromInput(value: String): PharmacyDispenseType? {
            return entries.firstOrNull {
                it.csvValue.equals(value.trim(), ignoreCase = true)
            }
        }
    }
}
