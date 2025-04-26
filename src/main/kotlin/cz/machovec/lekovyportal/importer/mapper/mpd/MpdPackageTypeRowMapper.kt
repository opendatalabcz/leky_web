package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdPackageType
import cz.machovec.lekovyportal.importer.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
import java.time.LocalDate

enum class MpdPackageTypeColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("OBAL")),
    NAME(listOf("NAZEV"), required = false),
    NAME_EN(listOf("NAZEV_EN"), required = false),
    EDQM_CODE(listOf("KOD_EDQM"), required = false);
}

class MpdPackageTypeRowMapper(
    private val validFrom: LocalDate
) : BaseSimpleRowMapper<MpdPackageTypeColumn, MpdPackageType>() {

    override fun map(row: CsvRow<MpdPackageTypeColumn>, rawLine: String): RowMappingResult<MpdPackageType> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdPackageTypeColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdPackageTypeColumn.CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val name = row[MpdPackageTypeColumn.NAME].safeTrim()
        val nameEn = row[MpdPackageTypeColumn.NAME_EN].safeTrim()
        val edqmCode = row[MpdPackageTypeColumn.EDQM_CODE]
            ?.trim()
            ?.toLongOrNull()

        /* ---------- entity construction ---------- */
        val entity = MpdPackageType(
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
