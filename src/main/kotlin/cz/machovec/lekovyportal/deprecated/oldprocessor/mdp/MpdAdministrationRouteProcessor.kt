package cz.machovec.lekovyportal.deprecated.oldprocessor.mdp

import cz.machovec.lekovyportal.core.domain.mpd.MpdAdministrationRoute
import cz.machovec.lekovyportal.core.domain.mpd.MpdDatasetType
import cz.machovec.lekovyportal.core.repository.mpd.MpdAdministrationRouteRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdAdministrationRouteProcessor(
    administrationRouteRepository: MpdAdministrationRouteRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository
) : BaseMpdProcessor<MpdAdministrationRoute>(
    administrationRouteRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {
    companion object {
        private const val COLUMN_CODE = "code"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_NAME_EN = "nameEn"
        private const val COLUMN_NAME_LAT = "nameLat"
        private const val COLUMN_EDQM_CODE = "edqmCode"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_ADMINISTRATION_ROUTE

    override fun getExpectedColumnsMap(): Map<String, List<String>> = mapOf(
        COLUMN_CODE to listOf("CESTA"),
        COLUMN_NAME to listOf("NAZEV"),
        COLUMN_NAME_EN to listOf("NAZEV_EN"),
        COLUMN_NAME_LAT to listOf("NAZEV_LAT"),
        COLUMN_EDQM_CODE to listOf("KOD_EDQM")
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdAdministrationRoute? {
        try {
            // Mandatory attributes
            val code = row[headerIndex.getValue(COLUMN_CODE)].trim()

            // Optional attributes
            val name = headerIndex[COLUMN_NAME]?.let { row.getOrNull(it)?.trim() }
            val nameEn = headerIndex[COLUMN_NAME_EN]?.let { row.getOrNull(it)?.trim() }
            val nameLat = headerIndex[COLUMN_NAME_LAT]?.let { row.getOrNull(it)?.trim() }
            val edqmCode = headerIndex[COLUMN_EDQM_CODE]
                ?.let { row.getOrNull(it)?.trim()?.toLongOrNull() }

            return MpdAdministrationRoute(
                firstSeen = importedDatasetValidFrom,
                missingSince = null,
                code = code,
                name = name,
                nameEn = nameEn,
                nameLat = nameLat,
                edqmCode = edqmCode
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse ${getDatasetType().description} row: ${row.joinToString()}" }
            return null
        }
    }
}
