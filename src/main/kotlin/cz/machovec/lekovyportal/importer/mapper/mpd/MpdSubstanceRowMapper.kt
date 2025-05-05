package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdSubstance
import cz.machovec.lekovyportal.importer.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
import cz.machovec.lekovyportal.importer.processing.mpd.MpdReferenceDataProvider
import java.time.LocalDate

enum class MpdSubstanceColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("KOD_LATKY")),
    SOURCE(listOf("ZDROJ"), required = false),
    NAME_INN(listOf("NAZEV_INN"), required = false),
    NAME_EN(listOf("NAZEV_EN"), required = false),
    NAME(listOf("NAZEV"), required = false),
    ADDICTION_CATEGORY(listOf("ZAV"), required = false),
    DOPING_CATEGORY(listOf("DOP"), required = false),
    GOVERNMENT_REGULATION_CATEGORY(listOf("NARVLA"), required = false);
}

class MpdSubstanceRowMapper(
    private val validFrom: LocalDate,
    private val refProvider: MpdReferenceDataProvider
) : BaseSimpleRowMapper<MpdSubstanceColumn, MpdSubstance>() {

    override fun map(row: CsvRow<MpdSubstanceColumn>, rawLine: String): RowMappingResult<MpdSubstance> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdSubstanceColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdSubstanceColumn.CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val sourceCode = row[MpdSubstanceColumn.SOURCE].safeTrim()
        val source = sourceCode?.let { refProvider.getSources()[it] }

        val nameInn = row[MpdSubstanceColumn.NAME_INN].safeTrim()
        val nameEn = row[MpdSubstanceColumn.NAME_EN].safeTrim()
        val name = row[MpdSubstanceColumn.NAME].safeTrim()

        val addictionCategoryCode = row[MpdSubstanceColumn.ADDICTION_CATEGORY].safeTrim()
        val addictionCategory = addictionCategoryCode?.let { refProvider.getAddictionCategories()[it] }

        val dopingCategoryCode = row[MpdSubstanceColumn.DOPING_CATEGORY].safeTrim()
        val dopingCategory = dopingCategoryCode?.let { refProvider.getDopingCategories()[it] }

        val govRegulationCategoryCode = row[MpdSubstanceColumn.GOVERNMENT_REGULATION_CATEGORY].safeTrim()
        val governmentRegulationCategory = govRegulationCategoryCode?.let { refProvider.getGovRegulationCategories()[it] }

        /* ---------- entity construction ---------- */
        val entity = MpdSubstance(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            source = source,
            nameInn = nameInn,
            nameEn = nameEn,
            name = name,
            addictionCategory = addictionCategory,
            dopingCategory = dopingCategory,
            governmentRegulationCategory = governmentRegulationCategory
        )

        return RowMappingResult.Success(entity)
    }
}
