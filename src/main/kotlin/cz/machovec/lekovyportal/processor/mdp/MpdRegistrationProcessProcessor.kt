package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdRegistrationProcess
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRegistrationProcessRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdRegistrationProcessProcessor(
    registrationProcessRepository: MpdRegistrationProcessRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository
) : BaseMpdProcessor<MpdRegistrationProcess>(
    registrationProcessRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {
    companion object {
        private const val COLUMN_KOD = "reg_proc"
        private const val COLUMN_NAZEV = "NAZEV"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_REGISTRATION_PROCESS

    override fun getExpectedColumns(): List<String> = listOf(
        COLUMN_KOD,
        COLUMN_NAZEV
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdRegistrationProcess? {
        try {
            // Mandatory attributes
            val code = row[headerIndex.getValue(COLUMN_KOD)].trim()

            // Optional attributes
            val name = headerIndex[COLUMN_NAZEV]
                ?.let { row.getOrNull(it)?.trim() }

            return MpdRegistrationProcess(
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
