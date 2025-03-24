package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDosageForm
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDosageFormRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdDosageFormProcessor(
    dosageFormRepository: MpdDosageFormRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository
) : BaseMpdProcessor<MpdDosageForm>(
    dosageFormRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {
    companion object {
        private const val COLUMN_FORMA = "FORMA"
        private const val COLUMN_NAZEV = "NAZEV"
        private const val COLUMN_NAZEV_EN = "NAZEV_EN"
        private const val COLUMN_NAZEV_LAT = "NAZEV_LAT"
        private const val COLUMN_JE_KONOPI = "JE_KONOPI"
        private const val COLUMN_KOD_EDQM = "KOD_EDQM"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_DOSAGE_FORM

    override fun getExpectedColumns(): List<String> = listOf(
        COLUMN_FORMA,
        COLUMN_NAZEV,
        COLUMN_NAZEV_EN,
        COLUMN_NAZEV_LAT,
        COLUMN_JE_KONOPI,
        COLUMN_KOD_EDQM
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdDosageForm? {
        try {
            // Mandatory attributes
            val code = row[headerIndex.getValue(COLUMN_FORMA)].trim()

            // Optional attributes
            val name = headerIndex[COLUMN_NAZEV]
                ?.let { row.getOrNull(it)?.trim() }
            val nameEn = headerIndex[COLUMN_NAZEV_EN]
                ?.let { row.getOrNull(it)?.trim() }
            val nameLat = headerIndex[COLUMN_NAZEV_LAT]
                ?.let { row.getOrNull(it)?.trim() }
            val isCannabis = headerIndex[COLUMN_JE_KONOPI]
                ?.let { row.getOrNull(it)?.trim().equals("A", ignoreCase = true) }
            val edqmCode = headerIndex[COLUMN_KOD_EDQM]
                ?.let { row.getOrNull(it)?.trim()?.toLongOrNull() }

            return MpdDosageForm(
                firstSeen = importedDatasetValidFrom,
                missingSince = null,
                code = code,
                name = name,
                nameEn = nameEn,
                nameLat = nameLat,
                isCannabis = isCannabis,
                edqmCode = edqmCode
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse ${getDatasetType().description} row: ${row.joinToString()}" }
            return null
        }
    }
}
