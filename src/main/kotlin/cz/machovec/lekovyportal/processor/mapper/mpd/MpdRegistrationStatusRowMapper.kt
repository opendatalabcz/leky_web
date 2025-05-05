package cz.machovec.lekovyportal.processor.mapper.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdRegistrationStatus
import cz.machovec.lekovyportal.processor.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.processor.mapper.ColumnAlias
import cz.machovec.lekovyportal.processor.mapper.CsvRow
import cz.machovec.lekovyportal.processor.mapper.FailureReason
import cz.machovec.lekovyportal.processor.mapper.RowFailure
import cz.machovec.lekovyportal.processor.mapper.RowMappingResult
import java.time.LocalDate

enum class MpdRegistrationStatusColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("REG")),
    NAME(listOf("NAZEV"), required = false);
}

class MpdRegistrationStatusRowMapper(
    private val validFrom: LocalDate
) : BaseSimpleRowMapper<MpdRegistrationStatusColumn, MpdRegistrationStatus>() {

    override fun map(row: CsvRow<MpdRegistrationStatusColumn>, rawLine: String): RowMappingResult<MpdRegistrationStatus> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdRegistrationStatusColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdRegistrationStatusColumn.CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val name = row[MpdRegistrationStatusColumn.NAME].safeTrim()

        /* ---------- entity construction ---------- */
        val entity = MpdRegistrationStatus(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            name = name
        )

        return RowMappingResult.Success(entity)
    }
}
