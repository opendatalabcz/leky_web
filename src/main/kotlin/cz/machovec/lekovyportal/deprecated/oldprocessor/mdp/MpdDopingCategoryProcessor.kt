package cz.machovec.lekovyportal.deprecated.oldprocessor.mdp

import cz.machovec.lekovyportal.core.domain.mpd.MpdDatasetType
import cz.machovec.lekovyportal.core.domain.mpd.MpdDopingCategory
import cz.machovec.lekovyportal.core.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdDopingCategoryRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdDopingCategoryProcessor(
    dopingCategoryRepository: MpdDopingCategoryRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository
) : BaseMpdProcessor<MpdDopingCategory>(
    dopingCategoryRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {
    companion object {
        private const val COLUMN_CODE = "code"
        private const val COLUMN_NAME = "name"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_DOPING_CATEGORY

    override fun getExpectedColumnsMap(): Map<String, List<String>> = mapOf(
        COLUMN_CODE to listOf("DOPING"),
        COLUMN_NAME to listOf("NAZEV")
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdDopingCategory? {
        try {
            // Mandatory attributes
            val code = row[headerIndex.getValue(COLUMN_CODE)].trim()

            // Optional attributes
            val name = headerIndex[COLUMN_NAME]?.let { row.getOrNull(it)?.trim() }

            return MpdDopingCategory(
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
