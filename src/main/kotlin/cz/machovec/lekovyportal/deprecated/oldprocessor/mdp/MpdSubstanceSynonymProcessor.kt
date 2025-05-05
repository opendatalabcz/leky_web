package cz.machovec.lekovyportal.deprecated.oldprocessor.mdp

import cz.machovec.lekovyportal.core.domain.mpd.MpdDatasetType
import cz.machovec.lekovyportal.core.domain.mpd.MpdSubstanceSynonym
import cz.machovec.lekovyportal.core.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdRecordTemporaryAbsenceRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdSubstanceSynonymRepository
import cz.machovec.lekovyportal.processor.processing.mpd.MpdReferenceDataProvider
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
        private const val COLUMN_SUBSTANCE = "substance"
        private const val COLUMN_SEQUENCE = "sequenceNumber"
        private const val COLUMN_SOURCE = "source"
        private const val COLUMN_NAME = "name"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_SUBSTANCE_SYNONYM

    override fun getExpectedColumnsMap(): Map<String, List<String>> = mapOf(
        COLUMN_SUBSTANCE to listOf("KOD_LATKY"),
        COLUMN_SEQUENCE to listOf("SQ"),
        COLUMN_SOURCE to listOf("ZDROJ"),
        COLUMN_NAME to listOf("NAZEV")
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
                    // logger.warn { "Unknown substance '$substanceCode', skipping row." }
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
