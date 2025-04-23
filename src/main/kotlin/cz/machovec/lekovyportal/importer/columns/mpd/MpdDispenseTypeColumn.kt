package cz.machovec.lekovyportal.importer.columns.mpd

import cz.machovec.lekovyportal.importer.columns.ColumnSpec

enum class MpdDispenseTypeColumn(val aliases: List<String>, val required: Boolean = true) {
    CODE(listOf("NAZEV")),
    NAME(listOf("VYDEJ"), required = false);

    fun toSpec() = ColumnSpec(this, aliases, required)
}