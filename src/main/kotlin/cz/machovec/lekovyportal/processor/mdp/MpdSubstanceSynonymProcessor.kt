package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdSubstanceSynonym
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdSubstanceSynonymRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdSubstanceSynonymProcessor(
    synonymRepository: MpdSubstanceSynonymRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository,
    private val referenceDataProvider: MpdReferenceDataProvider
) : BaseMpdProcessor<MpdSubstanceSynonym>(
    synonymRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {

    companion object {
        private const val COLUMN_SUBSTANCE = "SUBSTANCE_KOD"
        private const val COLUMN_SEQUENCE = "SEKVENCE"
        private const val COLUMN_SOURCE = "ZDROJ"
        private const val COLUMN_NAME = "JMENO"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_SUBSTANCE_SYNONYM

    override fun getExpectedColumns(): List<String> = listOf(
        COLUMN_SUBSTANCE,
        COLUMN_SEQUENCE,
        COLUMN_SOURCE,
        COLUMN_NAME
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdSubstanceSynonym? {
        try {
            // Mandatory: substance code
            val substanceCode = row[headerIndex.getValue(COLUMN_SUBSTANCE)].trim()
            val substance = referenceDataProvider.getSubstances()[substanceCode]
                ?: return null.also {
                    logger.warn { "Unknown substance '$substanceCode', skipping row." }
                }

            // Optional sequence number
            val sequenceNumber = headerIndex[COLUMN_SEQUENCE]?.let {
                row.getOrNull(it)?.trim()?.takeIf { it.isNotEmpty() }?.toIntOrNull()
            }

            // Mandatory: source
            val sourceCode = row[headerIndex.getValue(COLUMN_SOURCE)].trim()
            val source = referenceDataProvider.getSources()[sourceCode]
                ?: return null.also {
                    logger.warn { "Unknown source '$sourceCode', skipping row." }
                }

            // Optional name
            val name = headerIndex[COLUMN_NAME]?.let { row.getOrNull(it)?.trim() }

            return MpdSubstanceSynonym(
                firstSeen = importedDatasetValidFrom,
                missingSince = null,
                substance = substance,
                sequenceNumber = sequenceNumber,
                source = source,
                name = name
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse ${getDatasetType().description} row: ${row.joinToString()}" }
            return null
        }
    }
}
