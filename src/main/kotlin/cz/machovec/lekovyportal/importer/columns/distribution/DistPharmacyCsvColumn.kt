package cz.machovec.lekovyportal.importer.columns.distribution

import cz.machovec.lekovyportal.importer.columns.ColumnSpec

enum class DistPharmacyCsvColumn(val aliases: List<String>, val required: Boolean = true) {
    PERIOD          (listOf("Období")),
    DISPENSE_TYPE   (listOf("Typ hlášení")),
    SUKL_CODE       (listOf("Kód SÚKL")),
    PACKAGE_COUNT   (listOf("Počet balení"));

    fun toSpec() = ColumnSpec(this, aliases, required)
}
