package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProductComposition
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMedicinalProductCompositionRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdMedicinalProductCompositionProcessor(
    mpdMedicinalProductCompositionRepository: MpdMedicinalProductCompositionRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository,
    private val referenceDataProvider: MpdReferenceDataProvider
) : BaseMpdProcessor<MpdMedicinalProductComposition>(
    mpdMedicinalProductCompositionRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {

    companion object {
        private const val COLUMN_SUKL_CODE = "KOD_SUKL"
        private const val COLUMN_SUBSTANCE_CODE = "KOD_LATKY"
        private const val COLUMN_SEQUENCE = "SQ"
        private const val COLUMN_COMPOSITION_FLAG = "S"
        private const val COLUMN_AMOUNT_FROM = "AMNT_OD"
        private const val COLUMN_AMOUNT_TO = "AMNT"
        private const val COLUMN_MEASUREMENT_UNIT = "UN"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_SUBSTANCE_COMPOSITION

    override fun getExpectedColumnsMap(): Map<String, List<String>> = mapOf(
        COLUMN_SUKL_CODE to listOf(COLUMN_SUKL_CODE),
        COLUMN_SUBSTANCE_CODE to listOf(COLUMN_SUBSTANCE_CODE),
        COLUMN_SEQUENCE to listOf(COLUMN_SEQUENCE),
        COLUMN_COMPOSITION_FLAG to listOf(COLUMN_COMPOSITION_FLAG),
        COLUMN_AMOUNT_FROM to listOf(COLUMN_AMOUNT_FROM),
        COLUMN_AMOUNT_TO to listOf(COLUMN_AMOUNT_TO),
        COLUMN_MEASUREMENT_UNIT to listOf(COLUMN_MEASUREMENT_UNIT)
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdMedicinalProductComposition? {
        try {
            val suklCode = row[headerIndex.getValue(COLUMN_SUKL_CODE)].trim()
            val substanceCode = row[headerIndex.getValue(COLUMN_SUBSTANCE_CODE)].trim()

            val medicinalProduct = referenceDataProvider.getMedicinalProducts()[suklCode]
            val substance = referenceDataProvider.getSubstances()[substanceCode]

            if (medicinalProduct == null || substance == null) {
                logger.warn { "Unknown reference – suklCode: '$suklCode', substanceCode: '$substanceCode'" }
                return null
            }

            val sequenceNumber = headerIndex[COLUMN_SEQUENCE]?.let { row.getOrNull(it)?.toIntOrNull() }
            val compositionFlag = headerIndex[COLUMN_COMPOSITION_FLAG]?.let { row.getOrNull(it)?.trim() }
                ?.let { referenceDataProvider.getCompositionFlags()[it] }

            val amountFrom = headerIndex[COLUMN_AMOUNT_FROM]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }
            val amountTo = headerIndex[COLUMN_AMOUNT_TO]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }

            val measurementUnit = headerIndex[COLUMN_MEASUREMENT_UNIT]?.let { row.getOrNull(it)?.trim() }
                ?.let { referenceDataProvider.getMeasurementUnits()[it] }

            return MpdMedicinalProductComposition(
                firstSeen = importedDatasetValidFrom,
                missingSince = null,
                medicinalProduct = medicinalProduct,
                substance = substance,
                sequenceNumber = sequenceNumber,
                compositionFlag = compositionFlag,
                amountFrom = amountFrom,
                amountTo = amountTo,
                measurementUnit = measurementUnit
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to map row: ${row.joinToString()}" }
            return null
        }
    }
}

