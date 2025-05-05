package cz.machovec.lekovyportal.processor.mapper.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdDopingCategory
import cz.machovec.lekovyportal.processor.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.processor.mapper.ColumnAlias
import cz.machovec.lekovyportal.processor.mapper.CsvRow
import cz.machovec.lekovyportal.processor.mapper.FailureReason
import cz.machovec.lekovyportal.processor.mapper.RowFailure
import cz.machovec.lekovyportal.processor.mapper.RowMappingResult
import java.time.LocalDate

enum class MpdDopingCategoryColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("DOPING")),
    NAME(listOf("NAZEV"), required = false);
}

class MpdDopingCategoryRowMapper(
    private val validFrom: LocalDate
) : BaseSimpleRowMapper<MpdDopingCategoryColumn, MpdDopingCategory>() {

    override fun map(row: CsvRow<MpdDopingCategoryColumn>, rawLine: String): RowMappingResult<MpdDopingCategory> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdDopingCategoryColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdDopingCategoryColumn.CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val name = row[MpdDopingCategoryColumn.NAME].safeTrim()

        /* ---------- entity construction ---------- */
        val entity = MpdDopingCategory(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            name = name
        )

        return RowMappingResult.Success(entity)
    }
}

