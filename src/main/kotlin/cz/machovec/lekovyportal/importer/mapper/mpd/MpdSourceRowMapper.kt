package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdSource
import cz.machovec.lekovyportal.importer.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
import java.time.LocalDate

enum class MpdSourceColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("ZDROJ")),
    NAME(listOf("NAZEV"), required = false);
}

class MpdSourceRowMapper(
    private val validFrom: LocalDate
) : BaseSimpleRowMapper<MpdSourceColumn, MpdSource>() {

    override fun map(row: CsvRow<MpdSourceColumn>, rawLine: String): RowMappingResult<MpdSource> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdSourceColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdSourceColumn.CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val name = row[MpdSourceColumn.NAME].safeTrim()

        /* ---------- entity construction ---------- */
        val entity = MpdSource(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            name = name
        )

        return RowMappingResult.Success(entity)
    }
}
