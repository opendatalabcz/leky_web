package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdAtcGroup
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAtcGroupRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdAtcGroupProcessor(
    atcGroupRepository: MpdAtcGroupRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository
) : BaseMpdProcessor<MpdAtcGroup>(
    atcGroupRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {
    companion object {
        private const val COLUMN_ATC = "ATC"
        private const val COLUMN_TYP = "NT"
        private const val COLUMN_NAZEV = "NAZEV"
        private const val COLUMN_NAZEV_EN = "NAZEV_EN"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_ATC_GROUP

    override fun getExpectedColumns(): List<String> = listOf(
        COLUMN_ATC,
        COLUMN_TYP,
        COLUMN_NAZEV,
        COLUMN_NAZEV_EN
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdAtcGroup? {
        try {
            // Mandatory attributes
            val code = row[headerIndex.getValue(COLUMN_ATC)].trim()

            // Optional attributes
            val type = headerIndex[COLUMN_TYP]?.let { row.getOrNull(it)?.trim()?.firstOrNull() }
            val name = headerIndex[COLUMN_NAZEV]?.let { row.getOrNull(it)?.trim() }
            val nameEn = headerIndex[COLUMN_NAZEV_EN]?.let { row.getOrNull(it)?.trim() }

            return MpdAtcGroup(
                firstSeen = importedDatasetValidFrom,
                missingSince = null,
                code = code,
                type = type,
                name = name,
                nameEn = nameEn
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse ${getDatasetType().description} row: ${row.joinToString()}" }
            return null
        }
    }
}
