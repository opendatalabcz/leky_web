package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdCountry
import cz.machovec.lekovyportal.importer.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import java.time.LocalDate

enum class MpdCountryColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("ZEM")),
    NAME(listOf("NAZEV"), required = false),
    NAME_EN(listOf("NAZEV_EN"), required = false),
    EDQM_CODE(listOf("KOD_EDQM"), required = false);
}

class MpdCountryRowMapper(
    private val validFrom: LocalDate
) : BaseSimpleRowMapper<MpdCountryColumn, MpdCountry>() {

    override fun map(row: CsvRow<MpdCountryColumn>): MpdCountry? {
        val code = row[MpdCountryColumn.CODE].safeTrim() ?: return null

        return MpdCountry(
            firstSeen    = validFrom,
            missingSince = null,
            code         = code,
            name         = row[MpdCountryColumn.NAME].safeTrim(),
            nameEn       = row[MpdCountryColumn.NAME_EN].safeTrim(),
            edqmCode     = row[MpdCountryColumn.EDQM_CODE].safeTrim()
        )
    }
}
