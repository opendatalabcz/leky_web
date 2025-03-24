package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdMeasurementUnit
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMeasurementUnitRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdMeasurementUnitProcessor(
    measurementUnitRepository: MpdMeasurementUnitRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository
) : BaseMpdProcessor<MpdMeasurementUnit>(
    measurementUnitRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {
    companion object {
        private const val COLUMN_KOD = "jd"
        private const val COLUMN_NAZEV = "NAZEV"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_MEASUREMENT_UNIT

    override fun getExpectedColumns(): List<String> = listOf(
        COLUMN_KOD,
        COLUMN_NAZEV
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdMeasurementUnit? {
        try {
            // Mandatory attributes
            val code = row[headerIndex.getValue(COLUMN_KOD)].trim()

            // Optional attributes
            val name = headerIndex[COLUMN_NAZEV]
                ?.let { row.getOrNull(it)?.trim() }

            return MpdMeasurementUnit(
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
