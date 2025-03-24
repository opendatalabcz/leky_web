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
        private const val COLUMN_KOD = "KOD_LATKY"
        private const val COLUMN_INN = "NAZEV_INN"
        private const val COLUMN_EN = "NAZEV_EN"
        private const val COLUMN_NAZEV = "NAZEV"
        private const val COLUMN_ZAVISLOST = "ZAV"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_ACTIVE_SUBSTANCE

    override fun getExpectedColumns(): List<String> = listOf(
        COLUMN_KOD,
        COLUMN_INN,
        COLUMN_EN,
        COLUMN_NAZEV,
        COLUMN_ZAVISLOST
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdActiveSubstance? {
        try {
            // Mandatory attributes
            val code = row[headerIndex.getValue(COLUMN_KOD)].trim()

            // Optional attributes
            val nameInn = headerIndex[COLUMN_INN]?.let { row.getOrNull(it)?.trim() }
            val nameEn = headerIndex[COLUMN_EN]?.let { row.getOrNull(it)?.trim() }
            val name = headerIndex[COLUMN_NAZEV]?.let { row.getOrNull(it)?.trim() }
            val addictionCategoryCode = headerIndex[COLUMN_ZAVISLOST]?.let { row.getOrNull(it)?.trim() }

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

