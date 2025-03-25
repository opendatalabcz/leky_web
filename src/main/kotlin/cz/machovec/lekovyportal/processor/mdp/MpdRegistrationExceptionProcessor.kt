package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdRegistrationException
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRegistrationExceptionRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

@Service
class MpdRegistrationExceptionProcessor(
    registrationExceptionRepository: MpdRegistrationExceptionRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository,
    private val referenceDataProvider: MpdReferenceDataProvider
) : BaseMpdProcessor<MpdRegistrationException>(
    registrationExceptionRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {

    companion object {
        private const val COLUMN_SUKL_CODE = "suklCode"
        private const val COLUMN_VALID_FROM = "validFrom"
        private const val COLUMN_VALID_TO = "validTo"
        private const val COLUMN_ALLOWED_PACKAGE_COUNT = "allowedPackageCount"
        private const val COLUMN_PURPOSE = "purpose"
        private const val COLUMN_WORKPLACE = "workplace"
        private const val COLUMN_DISTRIBUTOR = "distributor"
        private const val COLUMN_NOTE = "note"
        private const val COLUMN_SUBMITTER = "submitter"
        private const val COLUMN_MANUFACTURER = "manufacturer"
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_REGISTRATION_EXCEPTION

    override fun getExpectedColumnsMap(): Map<String, List<String>> = mapOf(
        COLUMN_SUKL_CODE to listOf("KOD_SUKL"),
        COLUMN_VALID_FROM to listOf("DAT_OD"),
        COLUMN_VALID_TO to listOf("DAT_DO"),
        COLUMN_ALLOWED_PACKAGE_COUNT to listOf("POVOL_BALENI"),
        COLUMN_PURPOSE to listOf("UCEL"),
        COLUMN_WORKPLACE to listOf("PRACOVISTE"),
        COLUMN_DISTRIBUTOR to listOf("DISTRIBUTOR"),
        COLUMN_NOTE to listOf("POZNAMKA"),
        COLUMN_SUBMITTER to listOf("PREDKLADATEL"),
        COLUMN_MANUFACTURER to listOf("VYROBCE")
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdRegistrationException? {
        try {
            // Mandatory attributes
            val suklCode = row[headerIndex.getValue(COLUMN_SUKL_CODE)].trim()
            val medicinalProduct = referenceDataProvider.getMedicinalProducts()[suklCode]
                ?: return null.also {
                    logger.warn { "Unknown SUKL code '$suklCode' – skipping row." }
                }

            val validFromRaw = row[headerIndex.getValue(COLUMN_VALID_FROM)].trim()
            val validFrom = parseDate(validFromRaw) ?: return null

            // Optional attributes
            val validTo = headerIndex[COLUMN_VALID_TO]?.let {
                parseDate(row[it].trim())
            }

            val allowedPackageCount = headerIndex[COLUMN_ALLOWED_PACKAGE_COUNT]?.let {
                row[it].trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
            }

            fun getStringOrNull(column: String): String? =
                headerIndex[column]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }

            return MpdRegistrationException(
                firstSeen = importedDatasetValidFrom,
                missingSince = null,
                medicinalProduct = medicinalProduct,
                validFrom = validFrom,
                validTo = validTo,
                allowedPackageCount = allowedPackageCount,
                purpose = getStringOrNull(COLUMN_PURPOSE),
                workplace = getStringOrNull(COLUMN_WORKPLACE),
                distributor = getStringOrNull(COLUMN_DISTRIBUTOR),
                note = getStringOrNull(COLUMN_NOTE),
                submitter = getStringOrNull(COLUMN_SUBMITTER),
                manufacturer = getStringOrNull(COLUMN_MANUFACTURER)
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse ${getDatasetType().description} row: ${row.joinToString()}" }
            return null
        }
    }

    private fun parseDate(value: String): LocalDate? {
        if (value.isBlank()) return null
        return try {
            LocalDate.parse(value, dateFormatter)
        } catch (e: Exception) {
            logger.warn { "Failed to parse date: $value" }
            null
        }
    }
}
