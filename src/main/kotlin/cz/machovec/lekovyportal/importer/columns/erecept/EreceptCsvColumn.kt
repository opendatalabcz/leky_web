package cz.machovec.lekovyportal.importer.columns.erecept

import cz.machovec.lekovyportal.importer.columns.ColumnSpec

enum class EreceptCsvColumn(val aliases: List<String>, val required: Boolean = true) {
    DISTRICT_CODE(listOf("OKRES_KOD")),
    YEAR(listOf("ROK")),
    MONTH(listOf("MESIC")),
    SUKL_CODE(listOf("KOD_SUKL")),
    QUANTITY(listOf("MNOZSTVI"));

    fun toSpec() = ColumnSpec(this, aliases, required)
}
