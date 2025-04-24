package cz.machovec.lekovyportal.importer.columns.distribution

import cz.machovec.lekovyportal.importer.columns.ColumnSpec

enum class DistMahCsvColumn(val aliases: List<String>, val required: Boolean = true) {
    PERIOD(listOf("Období")),
    PURCHASER_TYPE(listOf("Typ hlášení")),
    SUKL_CODE(listOf("Kód SÚKL")),
    MOVEMENT_TYPE(listOf("Typ pohybu")),
    PACKAGE_COUNT(listOf("Počet balení"));

    fun toSpec() = ColumnSpec(this, aliases, required)
}
