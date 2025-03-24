package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdSubstance
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdSubstanceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdSubstanceProcessor(
    substanceRepository: MpdSubstanceRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository,
    private val referenceDataProvider: MpdReferenceDataProvider
) : BaseMpdProcessor<MpdSubstance>(
    substanceRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {

    companion object {
        private const val COLUMN_KOD = "KOD_LATKY"
        private const val COLUMN_ZDROJ = "ZDROJ"
        private const val COLUMN_INN = "NAZE_INN"
        private const val COLUMN_EN = "NAZEV_EN"
        private const val COLUMN_NAZEV = "NAZEV"
        private const val COLUMN_ZAVISLOST = "ZAV"
        private const val COLUMN_DOPING = "DOP"
        private const val COLUMN_VLADNI = "NARVLA"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_SUBSTANCE

    override fun getExpectedColumns(): List<String> = listOf(
        COLUMN_KOD,
        COLUMN_ZDROJ,
        COLUMN_INN,
        COLUMN_EN,
        COLUMN_NAZEV,
        COLUMN_ZAVISLOST,
        COLUMN_DOPING,
        COLUMN_VLADNI
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdSubstance? {
        try {
            // Mandatory attribute
            val code = row[headerIndex.getValue(COLUMN_KOD)].trim()

            // Optional attributes
            val sourceCode = headerIndex[COLUMN_ZDROJ]?.let { row.getOrNull(it)?.trim() }
            val source = sourceCode?.let { referenceDataProvider.getSources()[it] }

            val nameInn = headerIndex[COLUMN_INN]?.let { row.getOrNull(it)?.trim() }
            val nameEn = headerIndex[COLUMN_EN]?.let { row.getOrNull(it)?.trim() }
            val name = headerIndex[COLUMN_NAZEV]?.let { row.getOrNull(it)?.trim() }

            val addictionCategory = headerIndex[COLUMN_ZAVISLOST]
                ?.let { row.getOrNull(it)?.trim() }
                ?.takeIf { it.isNotEmpty() }
                ?.let { referenceDataProvider.getAddictionCategories()[it] }

            val dopingCategory = headerIndex[COLUMN_DOPING]
                ?.let { row.getOrNull(it)?.trim() }
                ?.takeIf { it.isNotEmpty() }
                ?.let { referenceDataProvider.getDopingCategories()[it] }

            val governmentRegulationCategory = headerIndex[COLUMN_VLADNI]
                ?.let { row.getOrNull(it)?.trim() }
                ?.takeIf { it.isNotEmpty() }
                ?.let { referenceDataProvider.getGovRegulationCategories()[it] }

            return MpdSubstance(
                firstSeen = importedDatasetValidFrom,
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
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse ${getDatasetType().description} row: ${row.joinToString()}" }
            return null
        }
    }
}
