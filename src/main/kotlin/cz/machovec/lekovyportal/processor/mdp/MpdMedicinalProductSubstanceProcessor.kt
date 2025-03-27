package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProductSubstance
import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProductSubstanceRelationType
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMedicinalProductSubstanceRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdMedicinalProductSubstanceProcessor(
    private val repository: MpdMedicinalProductSubstanceRepository,
    private val attributeChangeRepository: MpdAttributeChangeRepository,
    private val temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository,
    private val referenceDataProvider: MpdReferenceDataProvider
) : BaseMpdProcessor<MpdMedicinalProductSubstance>(
    repository, attributeChangeRepository, temporaryAbsenceRepository
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

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_MEDICINAL_PRODUCT_SUBSTANCE

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
    ): MpdMedicinalProductSubstance? {
        throw UnsupportedOperationException("This processor uses custom CSV processing.")
    }

    override fun processCsv(csvBytes: ByteArray, importedDatasetValidFrom: LocalDate, importedDatasetValidTo: LocalDate?) {
        val (headers, rows) = readCsv(csvBytes)
        val columnIndexMap = findColumnIndexes(headers)

        val rawData = rows.mapNotNull { row ->
            try {
                val suklCode = row[columnIndexMap.getValue(COLUMN_SUKL_CODE)].trim()
                val substanceCode = row.getOrNull(columnIndexMap[COLUMN_SUBSTANCE_CODE] ?: -1)?.trim()
                val compositionFlag = columnIndexMap[COLUMN_COMPOSITION_FLAG]?.let { row.getOrNull(it)?.trim() }

                if (compositionFlag == "X" && substanceCode.isNullOrBlank()) {
                    return@mapNotNull null
                }

                MpdSubstanceRowData(
                    suklCode = suklCode,
                    substanceCode = substanceCode,
                    substanceName = substanceCode,
                    sequenceNumber = columnIndexMap[COLUMN_SEQUENCE]?.let { row.getOrNull(it)?.toIntOrNull() },
                    compositionFlag = compositionFlag,
                    amountFrom = columnIndexMap[COLUMN_AMOUNT_FROM]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } },
                    amountTo = columnIndexMap[COLUMN_AMOUNT_TO]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } },
                    measurementUnitCode = columnIndexMap[COLUMN_MEASUREMENT_UNIT]?.let { row.getOrNull(it)?.trim() }
                )
            } catch (e: Exception) {
                logger.warn { "Failed to parse row: ${row.joinToString()}" }
                null
            }
        }.toMutableList()

        linkRows(rawData)

        val medicinalProducts = referenceDataProvider.getMedicinalProducts()
        val substances = referenceDataProvider.getSubstances()
        val compositionFlags = referenceDataProvider.getCompositionFlags()
        val units = referenceDataProvider.getMeasurementUnits()

        val entityMap = mutableMapOf<String, MpdMedicinalProductSubstance>()

        rawData
            .filter { it.compositionFlag != "Z" }
            .forEach { row ->
                val medicinalProduct = medicinalProducts[row.suklCode]
                val substance = row.substanceCode?.let { substances[it] }
                val measurementUnit = row.measurementUnitCode?.let { units[it] }
                val compositionFlag = row.compositionFlag?.let { compositionFlags[it] }

                if (medicinalProduct == null || substance == null) {
                    logger.warn { "Unknown reference for SUKL: ${row.suklCode}, substance: ${row.substanceCode}" }
                    return@forEach
                }

                val entity = MpdMedicinalProductSubstance(
                    firstSeen = importedDatasetValidFrom,
                    missingSince = null,
                    medicinalProduct = medicinalProduct,
                    substance = substance,
                    sequenceNumber = row.sequenceNumber,
                    compositionFlag = compositionFlag,
                    amountFrom = row.amountFrom,
                    amountTo = row.amountTo,
                    measurementUnit = measurementUnit
                )

                val key = "${medicinalProduct.id}-${substance.id}"
                entityMap[key] = entity
            }

        rawData
            .filter { it.compositionFlag != "Z" && it.relatedToSubstanceCode != null }
            .forEach { row ->
                val medProduct = medicinalProducts[row.suklCode] ?: return@forEach
                val substance = row.substanceCode?.let { substances[it] } ?: return@forEach
                val relatedSubstance = substances[row.relatedToSubstanceCode] ?: return@forEach

                val key = "${medProduct.id}-${substance.id}"
                val relatedKey = "${medProduct.id}-${relatedSubstance.id}"

                val entity = entityMap[key]
                val relatedTo = entityMap[relatedKey]

                if (entity != null && relatedTo != null) {
                    entityMap[key] = entity.copy(
                        relatedTo = relatedTo,
                        relationType = row.relationType
                    )
                }
            }

        val importedEntities = entityMap.values.toList()
        val existingRecords = repository.findAll()

        val changesResult = detectChanges(existingRecords, importedEntities, importedDatasetValidFrom)
        val missingResult = detectNewlyMissing(existingRecords, importedEntities, importedDatasetValidFrom)

        val allRecordsToSave = changesResult.recordsToSave + missingResult.newlyMissingRecords

        logger.info { "Persisting ${allRecordsToSave.size} entities to database..." }
        val timeStart = System.currentTimeMillis()
        repository.saveAll(allRecordsToSave)
        val timeEnd = System.currentTimeMillis()
        logger.info { "Database saveAll() done in ${timeEnd - timeStart} ms" }
        attributeChangeRepository.saveAll(changesResult.attributeChanges)
        temporaryAbsenceRepository.saveAll(changesResult.reactivations)

        logDetailedSummary(changesResult, missingResult)
    }

    private fun linkRows(data: MutableList<MpdSubstanceRowData>) {
        for (i in data.indices) {
            val current = data[i]
            if (current.compositionFlag != "Z") continue

            val prev = data.getOrNull(i - 1)
            val next = data.getOrNull(i + 1)

            if (prev == null || next == null) continue
            if (prev.suklCode != next.suklCode) continue

            val type = when {
                current.substanceName?.trim()?.uppercase()?.startsWith("OR") == true ->
                    MpdMedicinalProductSubstanceRelationType.OR
                current.substanceName?.trim()?.uppercase()?.startsWith("CORRESPONDING TO") == true ->
                    MpdMedicinalProductSubstanceRelationType.CORRESPONDING_TO
                else -> null
            }

            if (type != null) {
                prev.relatedToSubstanceCode = next.substanceCode
                prev.relationType = type
                next.relatedToSubstanceCode = prev.substanceCode
                next.relationType = when (type) {
                    MpdMedicinalProductSubstanceRelationType.CORRESPONDING_TO -> MpdMedicinalProductSubstanceRelationType.CORRESPONDED_BY
                    else -> type
                }
            }
        }
    }
}

data class MpdSubstanceRowData(
    val suklCode: String,
    val substanceCode: String?,
    val substanceName: String?,
    val sequenceNumber: Int?,
    val compositionFlag: String?,
    val amountFrom: String?,
    val amountTo: String?,
    val measurementUnitCode: String?,

    var relatedToSubstanceCode: String? = null,
    var relationType: MpdMedicinalProductSubstanceRelationType? = null
)
