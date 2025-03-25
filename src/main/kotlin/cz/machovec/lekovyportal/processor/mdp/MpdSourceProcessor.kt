package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdSource
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdSourceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdSourceProcessor(
    sourceRepository: MpdSourceRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository
) : BaseMpdProcessor<MpdSource>(
    sourceRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {
    companion object {
        private const val CODE = "code"
        private const val NAME = "name"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_SOURCE

    override fun getExpectedColumnsMap(): Map<String, List<String>> = mapOf(
        CODE to listOf("ZDROJ"),
        NAME to listOf("NAZEV")
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdSource? {
        try {
            val codeIndex = headerIndex["code"] ?: return null
            val code = row.getOrNull(codeIndex)?.trim().orEmpty()
            if (code.isBlank()) return null

            val name = headerIndex["name"]?.let { row.getOrNull(it)?.trim() }

            return MpdSource(
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
