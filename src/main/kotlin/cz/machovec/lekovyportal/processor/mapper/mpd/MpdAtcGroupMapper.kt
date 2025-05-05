package cz.machovec.lekovyportal.processor.mapper.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdAtcGroup
import cz.machovec.lekovyportal.processor.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.processor.mapper.ColumnAlias
import cz.machovec.lekovyportal.processor.mapper.CsvRow
import cz.machovec.lekovyportal.processor.mapper.FailureReason
import cz.machovec.lekovyportal.processor.mapper.RowFailure
import cz.machovec.lekovyportal.processor.mapper.RowMappingResult
import java.time.LocalDate

enum class MpdAtcGroupColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("ATC")),
    TYPE(listOf("NT"), required = false),
    NAME(listOf("NAZEV"), required = false),
    NAME_EN (listOf("NAZEV_EN"), required = false);
}

class MpdAtcGroupRowMapper(
    private val validFrom: LocalDate
) : BaseSimpleRowMapper<MpdAtcGroupColumn, MpdAtcGroup>() {

    override fun map(row: CsvRow<MpdAtcGroupColumn>, rawLine: String): RowMappingResult<MpdAtcGroup> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdAtcGroupColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdAtcGroupColumn.CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val type = row[MpdAtcGroupColumn.TYPE]?.trim()?.firstOrNull()
        val name = row[MpdAtcGroupColumn.NAME].safeTrim()
        val nameEn = row[MpdAtcGroupColumn.NAME_EN].safeTrim()

        /* ---------- entity construction ---------- */
        val entity = MpdAtcGroup(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            type = type,
            name = name,
            nameEn = nameEn
        )

        return RowMappingResult.Success(entity)
    }
}
