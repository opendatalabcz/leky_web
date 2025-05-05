package cz.machovec.lekovyportal.processor.mapper.mpd


import cz.machovec.lekovyportal.core.domain.mpd.MpdRegistrationProcess
import cz.machovec.lekovyportal.processor.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.processor.mapper.ColumnAlias
import cz.machovec.lekovyportal.processor.mapper.CsvRow
import cz.machovec.lekovyportal.processor.mapper.FailureReason
import cz.machovec.lekovyportal.processor.mapper.RowFailure
import cz.machovec.lekovyportal.processor.mapper.RowMappingResult
import java.time.LocalDate

enum class MpdRegistrationProcessColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("REG_PROC")),
    NAME(listOf("NAZEV"), required = false);
}

class MpdRegistrationProcessRowMapper(
    private val validFrom: LocalDate
) : BaseSimpleRowMapper<MpdRegistrationProcessColumn, MpdRegistrationProcess>() {

    override fun map(row: CsvRow<MpdRegistrationProcessColumn>, rawLine: String): RowMappingResult<MpdRegistrationProcess> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdRegistrationProcessColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdRegistrationProcessColumn.CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val name = row[MpdRegistrationProcessColumn.NAME].safeTrim()

        /* ---------- entity construction ---------- */
        val entity = MpdRegistrationProcess(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            name = name
        )

        return RowMappingResult.Success(entity)
    }
}
