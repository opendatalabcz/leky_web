package cz.machovec.lekovyportal.processor.mapper.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdSource
import cz.machovec.lekovyportal.processor.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.processor.mapper.ColumnAlias
import cz.machovec.lekovyportal.processor.mapper.CsvRow
import cz.machovec.lekovyportal.processor.mapper.FailureReason
import cz.machovec.lekovyportal.processor.mapper.RowFailure
import cz.machovec.lekovyportal.processor.mapper.RowMappingResult
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
