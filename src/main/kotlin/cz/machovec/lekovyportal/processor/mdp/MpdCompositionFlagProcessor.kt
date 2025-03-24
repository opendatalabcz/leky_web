package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdCompositionFlag
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCompositionFlagRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdCompositionFlagProcessor(
    compositionFlagRepository: MpdCompositionFlagRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository
) : BaseMpdProcessor<MpdCompositionFlag>(
    compositionFlagRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {
    companion object {
        private const val COLUMN_KOD = "S"
        private const val COLUMN_VYZNAM = "VYZNAM"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_COMPOSITION_FLAG

    override fun getExpectedColumns(): List<String> = listOf(
        COLUMN_KOD,
        COLUMN_VYZNAM
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdCompositionFlag? {
        try {
            // Mandatory attributes
            val code = row[headerIndex.getValue(COLUMN_KOD)].trim()

            // Optional attributes
            val meaning = headerIndex[COLUMN_VYZNAM]?.let { row.getOrNull(it)?.trim() }

            return MpdCompositionFlag(
                firstSeen = importedDatasetValidFrom,
                missingSince = null,
                code = code,
                meaning = meaning
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse ${getDatasetType().description} row: ${row.joinToString()}" }
            return null
        }
    }
}
