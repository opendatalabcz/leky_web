package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdGovernmentRegulationCategory
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdGovernmentRegulationCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdGovernmentRegulationCategoryProcessor(
    regulationCategoryRepository: MpdGovernmentRegulationCategoryRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository
) : BaseMpdProcessor<MpdGovernmentRegulationCategory>(
    regulationCategoryRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {
    companion object {
        private const val COLUMN_CODE = "code"
        private const val COLUMN_NAME = "name"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_GOVERNMENT_REGULATION_CATEGORY

    override fun getExpectedColumnsMap(): Map<String, List<String>> = mapOf(
        COLUMN_CODE to listOf("NARVLA"),
        COLUMN_NAME to listOf("NAZEV")
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdGovernmentRegulationCategory? {
        try {
            // Mandatory attributes
            val code = row[headerIndex.getValue(COLUMN_CODE)].trim()

            // Optional attributes
            val name = headerIndex[COLUMN_NAME]
                ?.let { row.getOrNull(it)?.trim() }

            return MpdGovernmentRegulationCategory(
                firstSeen = importedDatasetValidFrom,
                missingSince = null,
                code = code,
                name = name
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse ${getDatasetType().description} row: ${row.joinToString()}" }
            return null
        }
    }
}
