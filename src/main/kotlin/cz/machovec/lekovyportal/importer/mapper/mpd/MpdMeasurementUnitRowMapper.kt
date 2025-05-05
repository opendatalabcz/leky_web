package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdMeasurementUnit
import cz.machovec.lekovyportal.importer.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
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
