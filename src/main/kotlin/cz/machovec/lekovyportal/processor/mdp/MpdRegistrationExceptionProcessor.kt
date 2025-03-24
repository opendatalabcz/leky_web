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
        private const val COLUMN_SUKL_CODE = "KOD_SUKL"
        private const val COLUMN_VALID_FROM = "DAT_OD"
        private const val COLUMN_VALID_TO = "DAT_DO"
        private const val COLUMN_ALLOWED_PACKAGE_COUNT = "POVOL_BALENI"
        private const val COLUMN_PURPOSE = "UCEL"
        private const val COLUMN_WORKPLACE = "PRACOVISTE"
        private const val COLUMN_DISTRIBUTOR = "DISTRIBUTOR"
        private const val COLUMN_NOTE = "POZNAMKA"
        private const val COLUMN_SUBMITTER = "PREKLADATEL"
        private const val COLUMN_MANUFACTURER = "VYROBCE"
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_REGISTRATION_EXCEPTION

    override fun getExpectedColumns(): List<String> = listOf(
        COLUMN_SUKL_CODE,
        COLUMN_VALID_FROM,
        COLUMN_VALID_TO,
        COLUMN_ALLOWED_PACKAGE_COUNT,
        COLUMN_PURPOSE,
        COLUMN_WORKPLACE,
        COLUMN_DISTRIBUTOR,
        COLUMN_NOTE,
        COLUMN_SUBMITTER,
        COLUMN_MANUFACTURER
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
