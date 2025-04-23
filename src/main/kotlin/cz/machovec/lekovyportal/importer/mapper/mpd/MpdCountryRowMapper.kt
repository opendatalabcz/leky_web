package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdCountry
import cz.machovec.lekovyportal.importer.columns.mpd.MpdCountryColumn
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.RowMapper
import java.time.LocalDate

class MpdCountryRowMapper(
    private val validFrom: LocalDate
) : RowMapper<MpdCountryColumn, MpdCountry> {

    override fun map(row: CsvRow<MpdCountryColumn>): MpdCountry? {
        val code = row[MpdCountryColumn.CODE]?.takeIf { it.isNotBlank() } ?: return null

        return MpdCountry(
            firstSeen    = validFrom,
            missingSince = null,
            code         = code,
            name         = row[MpdCountryColumn.NAME],
            nameEn       = row[MpdCountryColumn.NAME_EN],
            edqmCode     = row[MpdCountryColumn.EDQM_CODE]
        )
    }
}
