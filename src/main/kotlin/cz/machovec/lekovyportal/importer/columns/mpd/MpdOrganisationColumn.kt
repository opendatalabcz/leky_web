package cz.machovec.lekovyportal.importer.columns.mpd

import cz.machovec.lekovyportal.importer.columns.ColumnSpec

enum class MpdOrganisationColumn(val aliases: List<String>, val required: Boolean = true) {
    CODE(listOf("ZKR_ORG")),
    COUNTRY(listOf("ZEM")),
    NAME(listOf("NAZEV"), required = false),
    IS_MANUFACTURER(listOf("VYROBCE"), required = false),
    IS_MARKETING_AUTH_HOLDER(listOf("DRZITEL"), required = false);

    fun toSpec() = ColumnSpec(this, aliases, required)
}
