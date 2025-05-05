package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdCompositionFlag
import cz.machovec.lekovyportal.importer.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
import java.time.LocalDate

enum class MpdCompositionFlagColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("S")),
    MEANING(listOf("VYZNAM"), required = false);
}

class MpdCompositionFlagRowMapper(
    private val validFrom: LocalDate
) : BaseSimpleRowMapper<MpdCompositionFlagColumn, MpdCompositionFlag>() {

    override fun map(row: CsvRow<MpdCompositionFlagColumn>, rawLine: String): RowMappingResult<MpdCompositionFlag> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdCompositionFlagColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdCompositionFlagColumn.CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val meaning = row[MpdCompositionFlagColumn.MEANING].safeTrim()

        /* ---------- entity construction ---------- */
        val entity = MpdCompositionFlag(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            meaning = meaning
        )

        return RowMappingResult.Success(entity)
    }
}
