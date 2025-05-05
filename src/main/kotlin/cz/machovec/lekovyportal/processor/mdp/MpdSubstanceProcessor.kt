package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdSubstance
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdSubstanceRepository
import cz.machovec.lekovyportal.importer.processing.mpd.MpdReferenceDataProvider
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
        private const val COLUMN_CODE = "code"
        private const val COLUMN_SOURCE = "source"
        private const val COLUMN_NAME_INN = "nameInn"
        private const val COLUMN_NAME_EN = "nameEn"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_ADDICTION_CATEGORY = "addictionCategory"
        private const val COLUMN_DOPING_CATEGORY = "dopingCategory"
        private const val COLUMN_GOV_REG = "governmentRegulationCategory"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_SUBSTANCE

    override fun getExpectedColumnsMap(): Map<String, List<String>> = mapOf(
        COLUMN_CODE to listOf("KOD_LATKY"),
        COLUMN_SOURCE to listOf("ZDROJ"),
        COLUMN_NAME_INN to listOf("NAZEV_INN"),
        COLUMN_NAME_EN to listOf("NAZEV_EN"),
        COLUMN_NAME to listOf("NAZEV"),
        COLUMN_ADDICTION_CATEGORY to listOf("ZAV"),
        COLUMN_DOPING_CATEGORY to listOf("DOP"),
        COLUMN_GOV_REG to listOf("NARVLA")
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdSubstance? {
        try {
            // Mandatory attribute
            val code = row[headerIndex.getValue(COLUMN_CODE)].trim()

            // Optional attributes
            val sourceCode = headerIndex[COLUMN_SOURCE]?.let { row.getOrNull(it)?.trim() }
            val source = sourceCode?.let { referenceDataProvider.getSources()[it] }

            val nameInn = headerIndex[COLUMN_NAME_INN]?.let { row.getOrNull(it)?.trim() }
            val nameEn = headerIndex[COLUMN_NAME_EN]?.let { row.getOrNull(it)?.trim() }
            val name = headerIndex[COLUMN_NAME]?.let { row.getOrNull(it)?.trim() }

            val addictionCategory = headerIndex[COLUMN_ADDICTION_CATEGORY]
                ?.let { row.getOrNull(it)?.trim() }
                ?.takeIf { it.isNotEmpty() }
                ?.let { referenceDataProvider.getAddictionCategories()[it] }

            val dopingCategory = headerIndex[COLUMN_DOPING_CATEGORY]
                ?.let { row.getOrNull(it)?.trim() }
                ?.takeIf { it.isNotEmpty() }
                ?.let { referenceDataProvider.getDopingCategories()[it] }

            val governmentRegulationCategory = headerIndex[COLUMN_GOV_REG]
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
