package cz.machovec.lekovyportal.deprecated.oldprocessor.mdp

import cz.machovec.lekovyportal.core.domain.mpd.MpdAddictionCategory
import cz.machovec.lekovyportal.core.domain.mpd.MpdDatasetType
import cz.machovec.lekovyportal.core.repository.mpd.MpdAddictionCategoryRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdAddictionCategoryProcessor(
    addictionCategoryRepository: MpdAddictionCategoryRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository
) : BaseMpdProcessor<MpdAddictionCategory>(
    addictionCategoryRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {
    companion object {
        private const val COLUMN_CODE = "code"
        private const val COLUMN_NAME = "name"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_ADDICTION_CATEGORY

    override fun getExpectedColumnsMap(): Map<String, List<String>> = mapOf(
        COLUMN_CODE to listOf("ZAV"),
        COLUMN_NAME to listOf("NAZEV")
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdAddictionCategory? {
        try {
            // Mandatory attributes
            val code = row[headerIndex.getValue(COLUMN_CODE)].trim()

            // Optional attributes
            val name = headerIndex[COLUMN_NAME]
                ?.let { row.getOrNull(it)?.trim() }

            return MpdAddictionCategory(
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
