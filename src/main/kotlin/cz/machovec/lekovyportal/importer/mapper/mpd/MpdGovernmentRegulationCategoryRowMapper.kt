package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdGovernmentRegulationCategory
import cz.machovec.lekovyportal.importer.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
import java.time.LocalDate

enum class MpdGovernmentRegulationCategoryColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("NARVLA")),
    NAME(listOf("NAZEV"), required = false);
}

class MpdGovernmentRegulationCategoryRowMapper(
    private val validFrom: LocalDate
) : BaseSimpleRowMapper<MpdGovernmentRegulationCategoryColumn, MpdGovernmentRegulationCategory>() {

    override fun map(row: CsvRow<MpdGovernmentRegulationCategoryColumn>, rawLine: String): RowMappingResult<MpdGovernmentRegulationCategory> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdGovernmentRegulationCategoryColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdGovernmentRegulationCategoryColumn.CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val name = row[MpdGovernmentRegulationCategoryColumn.NAME].safeTrim()

        /* ---------- entity construction ---------- */
        val entity = MpdGovernmentRegulationCategory(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            name = name
        )

        return RowMappingResult.Success(entity)
    }
}

