package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDispenseType
import cz.machovec.lekovyportal.importer.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
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

    override fun map(row: CsvRow<MpdDispenseTypeColumn>, rawLine: String): RowMappingResult<MpdDispenseType> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdDispenseTypeColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdDispenseTypeColumn.CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val name = row[MpdDispenseTypeColumn.NAME].safeTrim()

        /* ---------- entity construction ---------- */
        val entity = MpdDispenseType(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            name = name
        )

        return RowMappingResult.Success(entity)
    }
}
