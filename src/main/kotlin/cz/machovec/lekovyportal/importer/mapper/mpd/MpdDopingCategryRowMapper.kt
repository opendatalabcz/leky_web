package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDopingCategory
import cz.machovec.lekovyportal.importer.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
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

