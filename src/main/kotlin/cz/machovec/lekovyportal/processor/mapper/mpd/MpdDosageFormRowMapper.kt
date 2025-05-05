package cz.machovec.lekovyportal.processor.mapper.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdDosageForm
import cz.machovec.lekovyportal.processor.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.processor.mapper.ColumnAlias
import cz.machovec.lekovyportal.processor.mapper.CsvRow
import cz.machovec.lekovyportal.processor.mapper.FailureReason
import cz.machovec.lekovyportal.processor.mapper.RowFailure
import cz.machovec.lekovyportal.processor.mapper.RowMappingResult
import java.time.LocalDate

enum class MpdDosageFormColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("FORMA")),
    NAME(listOf("NAZEV"), required = false),
    NAME_EN(listOf("NAZEV_EN"), required = false),
    NAME_LAT(listOf("NAZEV_LAT"), required = false),
    IS_CANNABIS(listOf("JE_KONOPI"), required = false),
    EDQM_CODE(listOf("KOD_EDQM"), required = false);
}

class MpdDosageFormRowMapper(
    private val validFrom: LocalDate
) : BaseSimpleRowMapper<MpdDosageFormColumn, MpdDosageForm>() {

    override fun map(row: CsvRow<MpdDosageFormColumn>, rawLine: String): RowMappingResult<MpdDosageForm> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdDosageFormColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdDosageFormColumn.CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val name = row[MpdDosageFormColumn.NAME].safeTrim()
        val nameEn = row[MpdDosageFormColumn.NAME_EN].safeTrim()
        val nameLat = row[MpdDosageFormColumn.NAME_LAT].safeTrim()
        val isCannabis = row[MpdDosageFormColumn.IS_CANNABIS]
            ?.trim()
            ?.equals("A", ignoreCase = true)
        val edqmCode = row[MpdDosageFormColumn.EDQM_CODE]
            ?.trim()
            ?.toLongOrNull()

        /* ---------- entity construction ---------- */
        val entity = MpdDosageForm(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            name = name,
            nameEn = nameEn,
            nameLat = nameLat,
            isCannabis = isCannabis,
            edqmCode = edqmCode
        )

        return RowMappingResult.Success(entity)
    }
}
