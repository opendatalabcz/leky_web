package cz.machovec.lekovyportal.importer.columns.mpd

import cz.machovec.lekovyportal.importer.columns.ColumnSpec

enum class MpdCountryColumn(val aliases: List<String>, val required: Boolean = true) {
    CODE(listOf("ZEM")),
    NAME(listOf("NAZEV"), required = false),
    NAME_EN(listOf("NAZEV_EN"), required = false),
    EDQM_CODE(listOf("KOD_EDQM"), required = false);

    fun toSpec() = ColumnSpec(this, aliases, required)
}