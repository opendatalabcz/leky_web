package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdIndicationGroup
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdIndicationGroupRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdIndicationGroupProcessor(
    indicationGroupRepository: MpdIndicationGroupRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository
) : BaseMpdProcessor<MpdIndicationGroup>(
    indicationGroupRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {
    companion object {
        private const val COLUMN_KOD = "indsk"
        private const val COLUMN_NAZEV = "NAZEV"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_INDICATION_GROUP

    override fun getExpectedColumns(): List<String> = listOf(
        COLUMN_KOD,
        COLUMN_NAZEV
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdIndicationGroup? {
        try {
            // Mandatory attributes
            val code = row[headerIndex.getValue(COLUMN_KOD)].trim()

            // Optional attributes
            val name = headerIndex[COLUMN_NAZEV]
                ?.let { row.getOrNull(it)?.trim() }

            return MpdIndicationGroup(
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
