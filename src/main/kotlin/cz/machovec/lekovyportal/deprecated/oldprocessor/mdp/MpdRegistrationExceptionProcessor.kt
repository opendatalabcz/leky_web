package cz.machovec.lekovyportal.deprecated.oldprocessor.mdp

import cz.machovec.lekovyportal.core.domain.mpd.MpdDatasetType
import cz.machovec.lekovyportal.core.domain.mpd.MpdRegistrationException
import cz.machovec.lekovyportal.core.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdRecordTemporaryAbsenceRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdRegistrationExceptionRepository
import cz.machovec.lekovyportal.processor.processing.mpd.MpdReferenceDataProvider
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
) : cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.BaseMpdProcessor<MpdRegistrationException>(
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
        cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_SUKL_CODE to listOf("KOD_SUKL"),
        cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_VALID_FROM to listOf("DATOD", "DAT_OD"),
        cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_VALID_TO to listOf("DATDO", "DAT_DO"),
        cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_ALLOWED_PACKAGE_COUNT to listOf("POVOL_BALENI"),
        cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_PURPOSE to listOf("UCEL"),
        cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_WORKPLACE to listOf("PRACOVISTE"),
        cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_DISTRIBUTOR to listOf("DISTRIBUTOR"),
        cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_NOTE to listOf("POZNAMKA"),
        cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_SUBMITTER to listOf("PREDKLADATEL"),
        cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_MANUFACTURER to listOf("VYROBCE")
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdRegistrationException? {
        try {
            // Mandatory attributes
            val suklCode = row[headerIndex.getValue(cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_SUKL_CODE)].trim()
            val medicinalProduct = referenceDataProvider.getMedicinalProducts()[suklCode]
                ?: return null.also {
                    cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.logger.warn { "Unknown SUKL code '$suklCode' – skipping row." }
                }

            val validFromRaw = row[headerIndex.getValue(cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_VALID_FROM)].trim()
            val validFrom = parseDate(validFromRaw) ?: return null

            // Optional attributes
            val validTo = headerIndex[cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_VALID_TO]?.let {
                parseDate(row[it].trim())
            }

            val allowedPackageCount = headerIndex[cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_ALLOWED_PACKAGE_COUNT]?.let {
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
                purpose = getStringOrNull(cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_PURPOSE),
                workplace = getStringOrNull(cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_WORKPLACE),
                distributor = getStringOrNull(cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_DISTRIBUTOR),
                note = getStringOrNull(cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_NOTE),
                submitter = getStringOrNull(cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_SUBMITTER),
                manufacturer = getStringOrNull(cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.MpdRegistrationExceptionProcessor.Companion.COLUMN_MANUFACTURER)
            )
        } catch (e: Exception) {
            cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.logger.error(e) { "Failed to parse ${getDatasetType().description} row: ${row.joinToString()}" }
            return null
        }
    }

    private fun parseDate(value: String): LocalDate? {
        if (value.isBlank()) return null
        return try {
            LocalDate.parse(value, dateFormatter)
        } catch (e: Exception) {
            cz.machovec.lekovyportal.deprecated.oldprocessor.mdp.logger.warn { "Failed to parse date: $value" }
            null
        }
    }
}
