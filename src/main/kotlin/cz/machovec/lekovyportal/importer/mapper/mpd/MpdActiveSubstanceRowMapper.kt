package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdActiveSubstance
import cz.machovec.lekovyportal.importer.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
import cz.machovec.lekovyportal.importer.processing.mpd.MpdReferenceDataProvider
import java.time.LocalDate

enum class MpdActiveSubstanceColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("KOD_LATKY")),
    NAME_INN(listOf("NAZEV_INN"), required = false),
    NAME_EN(listOf("NAZEV_EN"), required = false),
    NAME(listOf("NAZEV"), required = false),
    ADDICTION_CATEGORY_CODE(listOf("ZAV"), required = false);
}

class MpdActiveSubstanceRowMapper(
    private val validFrom: LocalDate,
    private val referenceDataProvider: MpdReferenceDataProvider
) : BaseSimpleRowMapper<MpdActiveSubstanceColumn, MpdActiveSubstance>() {

    override fun map(row: CsvRow<MpdActiveSubstanceColumn>, rawLine: String): RowMappingResult<MpdActiveSubstance> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdActiveSubstanceColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdActiveSubstanceColumn.CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val nameInn = row[MpdActiveSubstanceColumn.NAME_INN].safeTrim()
        val nameEn = row[MpdActiveSubstanceColumn.NAME_EN].safeTrim()
        val name = row[MpdActiveSubstanceColumn.NAME].safeTrim()
        val addictionCategoryCode = row[MpdActiveSubstanceColumn.ADDICTION_CATEGORY_CODE].safeTrim()

        val addictionCategory = addictionCategoryCode?.let {
            referenceDataProvider.getAddictionCategories()[it]
        }

        /* ---------- entity construction ---------- */
        val entity = MpdActiveSubstance(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            nameInn = nameInn,
            nameEn = nameEn,
            name = name,
            addictionCategory = addictionCategory
        )

        return RowMappingResult.Success(entity)
    }
}
