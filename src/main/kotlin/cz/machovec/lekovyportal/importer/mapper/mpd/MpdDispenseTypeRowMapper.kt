package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDispenseType
import cz.machovec.lekovyportal.importer.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import java.time.LocalDate

enum class MpdDispenseTypeColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("NAZEV")),
    NAME(listOf("VYDEJ"), required = false);
}

class MpdDispenseTypeRowMapper(
    private val validFrom: LocalDate
) : BaseSimpleRowMapper<MpdDispenseTypeColumn, MpdDispenseType>() {

    override fun map(row: CsvRow<MpdDispenseTypeColumn>): MpdDispenseType? {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdDispenseTypeColumn.CODE].safeTrim() ?: return null

        /* ---------- optional attributes ---------- */
        val name = row[MpdDispenseTypeColumn.NAME].safeTrim()

        /* ---------- entity construction ---------- */
        return MpdDispenseType(
            firstSeen    = validFrom,
            missingSince = null,
            code         = code,
            name         = name
        )
    }
}
