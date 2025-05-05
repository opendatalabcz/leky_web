package cz.machovec.lekovyportal.processor.mapper.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdAddictionCategory
import cz.machovec.lekovyportal.processor.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.processor.mapper.ColumnAlias
import cz.machovec.lekovyportal.processor.mapper.CsvRow
import cz.machovec.lekovyportal.processor.mapper.FailureReason
import cz.machovec.lekovyportal.processor.mapper.RowFailure
import cz.machovec.lekovyportal.processor.mapper.RowMappingResult
import java.time.LocalDate

enum class MpdAddictionCategoryColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("ZAV")),
    NAME(listOf("NAZEV"), required = false);
}

class MpdAddictionCategoryRowMapper(
    private val validFrom: LocalDate
) : BaseSimpleRowMapper<MpdAddictionCategoryColumn, MpdAddictionCategory>() {

    override fun map(row: CsvRow<MpdAddictionCategoryColumn>, rawLine: String): RowMappingResult<MpdAddictionCategory> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdAddictionCategoryColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdAddictionCategoryColumn.CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val name = row[MpdAddictionCategoryColumn.NAME].safeTrim()

        /* ---------- entity construction ---------- */
        val entity = MpdAddictionCategory(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            name = name
        )

        return RowMappingResult.Success(entity)
    }
}
