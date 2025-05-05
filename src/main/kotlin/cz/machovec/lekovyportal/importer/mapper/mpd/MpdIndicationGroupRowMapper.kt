package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdIndicationGroup
import cz.machovec.lekovyportal.importer.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
import java.time.LocalDate

enum class MpdIndicationGroupColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("INDSK")),
    NAME(listOf("NAZEV"), required = false);
}

class MpdIndicationGroupRowMapper(
    private val validFrom: LocalDate
) : BaseSimpleRowMapper<MpdIndicationGroupColumn, MpdIndicationGroup>() {

    override fun map(row: CsvRow<MpdIndicationGroupColumn>, rawLine: String): RowMappingResult<MpdIndicationGroup> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdIndicationGroupColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdIndicationGroupColumn.CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val name = row[MpdIndicationGroupColumn.NAME].safeTrim()

        /* ---------- entity construction ---------- */
        val entity = MpdIndicationGroup(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            name = name
        )

        return RowMappingResult.Success(entity)
    }
}
