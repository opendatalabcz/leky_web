package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDispenseType
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.RowMapper
import cz.machovec.lekovyportal.importer.columns.mpd.MpdDispenseTypeColumn
import java.time.LocalDate

class MpdDispenseTypeRowMapper(
    private val validFrom: LocalDate
) : RowMapper<MpdDispenseTypeColumn, MpdDispenseType> {

    override fun map(row: CsvRow<MpdDispenseTypeColumn>): MpdDispenseType? {
        val code = row[MpdDispenseTypeColumn.CODE]?.takeIf { it.isNotBlank() } ?: return null

        return MpdDispenseType(
            firstSeen    = validFrom,
            missingSince = null,
            code         = code,
            name         = row[MpdDispenseTypeColumn.NAME]
        )
    }
}