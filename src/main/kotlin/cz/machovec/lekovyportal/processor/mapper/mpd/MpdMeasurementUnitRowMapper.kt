package cz.machovec.lekovyportal.processor.mapper.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdMeasurementUnit
import cz.machovec.lekovyportal.processor.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.processor.mapper.ColumnAlias
import cz.machovec.lekovyportal.processor.mapper.CsvRow
import cz.machovec.lekovyportal.processor.mapper.FailureReason
import cz.machovec.lekovyportal.processor.mapper.RowFailure
import cz.machovec.lekovyportal.processor.mapper.RowMappingResult
import java.time.LocalDate

enum class MpdMeasurementUnitColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("JD", "UN")),
    NAME(listOf("NAZEV"), required = false);
}

class MpdMeasurementUnitRowMapper(
    private val validFrom: LocalDate
) : BaseSimpleRowMapper<MpdMeasurementUnitColumn, MpdMeasurementUnit>() {

    override fun map(row: CsvRow<MpdMeasurementUnitColumn>, rawLine: String): RowMappingResult<MpdMeasurementUnit> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdMeasurementUnitColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdMeasurementUnitColumn.CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val name = row[MpdMeasurementUnitColumn.NAME].safeTrim()

        /* ---------- entity construction ---------- */
        val entity = MpdMeasurementUnit(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            name = name
        )

        return RowMappingResult.Success(entity)
    }
}
