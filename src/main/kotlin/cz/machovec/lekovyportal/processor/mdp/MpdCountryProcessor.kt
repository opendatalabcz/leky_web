package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdCountry
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCountryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdCountryProcessor(
    countryRepository: MpdCountryRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository
) : BaseMpdProcessor<MpdCountry>(
    countryRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {
    companion object {
        private const val COLUMN_KOD = "ZEM"
        private const val COLUMN_NAZEV = "NAZEV"
        private const val COLUMN_NAZEV_EN = "NAZEV_EN"
        private const val COLUMN_KOD_EDQM = "KOD_EDQM"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_COUNTRY

    override fun getExpectedColumns(): List<String> = listOf(
        COLUMN_KOD,
        COLUMN_NAZEV,
        COLUMN_NAZEV_EN,
        COLUMN_KOD_EDQM
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdCountry? {
        try {
            // Mandatory attributes
            val code = row[headerIndex.getValue(COLUMN_KOD)].trim()

            // Optional attributes
            val name = headerIndex[COLUMN_NAZEV]?.let { row.getOrNull(it)?.trim() }
            val nameEn = headerIndex[COLUMN_NAZEV_EN]?.let { row.getOrNull(it)?.trim() }
            val edqmCode = headerIndex[COLUMN_KOD_EDQM]?.let { row.getOrNull(it)?.trim() }

            return MpdCountry(
                firstSeen = importedDatasetValidFrom,
                missingSince = null,
                code = code,
                name = name,
                nameEn = nameEn,
                edqmCode = edqmCode
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse ${getDatasetType().description} row: ${row.joinToString()}" }
            return null
        }
    }
}
