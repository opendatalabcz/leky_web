package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdPackageType
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdPackageTypeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdPackageTypeProcessor(
    packageTypeRepository: MpdPackageTypeRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository
) : BaseMpdProcessor<MpdPackageType>(
    packageTypeRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {
    companion object {
        private const val COLUMN_CODE = "code"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_NAME_EN = "nameEn"
        private const val COLUMN_EDQM_CODE = "edqmCode"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_PACKAGE_TYPE

    override fun getExpectedColumnsMap(): Map<String, List<String>> = mapOf(
        COLUMN_CODE to listOf("OBAL"),
        COLUMN_NAME to listOf("NAZEV"),
        COLUMN_NAME_EN to listOf("NAZEV_EN"),
        COLUMN_EDQM_CODE to listOf("KOD_EDQM")
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdPackageType? {
        try {
            // Mandatory attributes
            val code = row[headerIndex.getValue(COLUMN_CODE)].trim()

            // Optional attributes
            val name = headerIndex[COLUMN_NAME]?.let { row.getOrNull(it)?.trim() }
            val nameEn = headerIndex[COLUMN_NAME_EN]?.let { row.getOrNull(it)?.trim() }
            val edqmCode = headerIndex[COLUMN_EDQM_CODE]?.let { row.getOrNull(it)?.trim()?.toLongOrNull() }

            return MpdPackageType(
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
