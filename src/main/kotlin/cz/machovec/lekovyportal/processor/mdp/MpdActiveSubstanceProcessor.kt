package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdActiveSubstance
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.repository.mpd.MpdActiveSubstanceRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdActiveSubstanceProcessor(
    activeSubstanceRepository: MpdActiveSubstanceRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository,
    private val referenceDataProvider: MpdReferenceDataProvider
) : BaseMpdProcessor<MpdActiveSubstance>(
    activeSubstanceRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {

    companion object {
        private const val COLUMN_CODE = "code"
        private const val COLUMN_NAME_INN = "nameInn"
        private const val COLUMN_NAME_EN = "nameEn"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_ADDICTION_CATEGORY = "addictionCategoryCode"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_ACTIVE_SUBSTANCE

    override fun getExpectedColumnsMap(): Map<String, List<String>> = mapOf(
        COLUMN_CODE to listOf("KOD_LATKY"),
        COLUMN_NAME_INN to listOf("NAZEV_INN"),
        COLUMN_NAME_EN to listOf("NAZEV_EN"),
        COLUMN_NAME to listOf("NAZEV"),
        COLUMN_ADDICTION_CATEGORY to listOf("ZAV")
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdActiveSubstance? {
        try {
            // Mandatory attributes
            val code = row[headerIndex.getValue(COLUMN_CODE)].trim()

            // Optional attributes
            val nameInn = headerIndex[COLUMN_NAME_INN]?.let { row.getOrNull(it)?.trim() }
            val nameEn = headerIndex[COLUMN_NAME_EN]?.let { row.getOrNull(it)?.trim() }
            val name = headerIndex[COLUMN_NAME]?.let { row.getOrNull(it)?.trim() }
            val addictionCategoryCode = headerIndex[COLUMN_ADDICTION_CATEGORY]?.let { row.getOrNull(it)?.trim() }

            val addictionCategory = addictionCategoryCode?.let {
                referenceDataProvider.getAddictionCategories()[it]
            }

            return MpdActiveSubstance(
                firstSeen = importedDatasetValidFrom,
                missingSince = null,
                code = code,
                nameInn = nameInn,
                nameEn = nameEn,
                name = name,
                addictionCategory = addictionCategory
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse ${getDatasetType().description} row: ${row.joinToString()}" }
            return null
        }
    }
}
