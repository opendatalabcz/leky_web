package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdCountry
import cz.machovec.lekovyportal.importer.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
import java.time.LocalDate

enum class MpdCountryColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("ZEM")),
    NAME(listOf("NAZEV"), required = false),
    NAME_EN(listOf("NAZEV_EN"), required = false),
    EDQM_CODE(listOf("KOD_EDQM"), required = false);
}

class MpdCountryRowMapper(
    private val validFrom: LocalDate
) : BaseSimpleRowMapper<MpdCountryColumn, MpdCountry>() {

    override fun map(row: CsvRow<MpdCountryColumn>, rawLine: String): RowMappingResult<MpdCountry> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdCountryColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdCountryColumn.CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val name = row[MpdCountryColumn.NAME].safeTrim()
        val nameEn = row[MpdCountryColumn.NAME_EN].safeTrim()
        val edqmCode = row[MpdCountryColumn.EDQM_CODE].safeTrim()

        /* ---------- entity construction ---------- */
        val entity = MpdCountry(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            name = name,
            nameEn = nameEn,
            edqmCode = edqmCode
        )

        return RowMappingResult.Success(entity)
    }
}
