package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdAdministrationRoute
import cz.machovec.lekovyportal.importer.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
import java.time.LocalDate

enum class MpdAdministrationRouteColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("CESTA")),
    NAME(listOf("NAZEV"), required = false),
    NAME_EN(listOf("NAZEV_EN"), required = false),
    NAME_LAT(listOf("NAZEV_LAT"), required = false),
    EDQM_CODE(listOf("KOD_EDQM"), required = false);
}

class MpdAdministrationRouteRowMapper(
    private val validFrom: LocalDate
) : BaseSimpleRowMapper<MpdAdministrationRouteColumn, MpdAdministrationRoute>() {

    override fun map(row: CsvRow<MpdAdministrationRouteColumn>, rawLine: String): RowMappingResult<MpdAdministrationRoute> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdAdministrationRouteColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdAdministrationRouteColumn.CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val name = row[MpdAdministrationRouteColumn.NAME].safeTrim()
        val nameEn = row[MpdAdministrationRouteColumn.NAME_EN].safeTrim()
        val nameLat = row[MpdAdministrationRouteColumn.NAME_LAT].safeTrim()
        val edqmCode = row[MpdAdministrationRouteColumn.EDQM_CODE]?.trim()?.toLongOrNull()

        /* ---------- entity construction ---------- */
        val entity = MpdAdministrationRoute(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            name = name,
            nameEn = nameEn,
            nameLat = nameLat,
            edqmCode = edqmCode
        )

        return RowMappingResult.Success(entity)
    }
}
