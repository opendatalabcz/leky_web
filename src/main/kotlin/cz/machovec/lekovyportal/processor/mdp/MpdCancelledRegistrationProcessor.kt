package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.BaseMpdEntity
import cz.machovec.lekovyportal.domain.entity.mpd.MpdAdministrationRoute
import cz.machovec.lekovyportal.domain.entity.mpd.MpdCancelledRegistration
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDosageForm
import cz.machovec.lekovyportal.domain.entity.mpd.MpdRegistrationProcess
import cz.machovec.lekovyportal.domain.entity.mpd.MpdRegistrationStatus
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCancelledRegistrationRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

@Service
class MpdCancelledRegistrationProcessor(
    cancelledRegistrationRepository: MpdCancelledRegistrationRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository,
    private val referenceDataProvider: MpdReferenceDataProvider
) : BaseMpdProcessor<MpdCancelledRegistration>(
    cancelledRegistrationRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {

    companion object {
        private const val COLUMN_NAME = "name"
        private const val COLUMN_ADMINISTRATION_ROUTE = "administrationRoute"
        private const val COLUMN_DOSAGE_FORM = "dosageForm"
        private const val COLUMN_STRENGTH = "strength"
        private const val COLUMN_REGISTRATION_NUMBER = "registrationNumber"
        private const val COLUMN_PARALLEL_IMPORT_ID = "parallelImportId"
        private const val COLUMN_MRP_NUMBER = "mrpNumber"
        private const val COLUMN_REGISTRATION_PROCESS = "registrationProcess"
        private const val COLUMN_REGISTRATION_LEGAL_BASIS = "registrationLegalBasis"
        private const val COLUMN_MAH_CODE = "mahCode"
        private const val COLUMN_MAH_COUNTRY_CODE = "mahCountryCode"
        private const val COLUMN_REGISTRATION_END_DATE = "registrationEndDate"
        private const val COLUMN_REGISTRATION_STATUS = "registrationStatus"
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_CANCELLED_REGISTRATION

    override fun getExpectedColumnsMap(): Map<String, List<String>> = mapOf(
        COLUMN_NAME to listOf("NAZEV"),
        COLUMN_ADMINISTRATION_ROUTE to listOf("CESTA"),
        COLUMN_DOSAGE_FORM to listOf("FORMA"),
        COLUMN_STRENGTH to listOf("SILA"),
        COLUMN_REGISTRATION_NUMBER to listOf("REGISTRACNI_CISLO"),
        COLUMN_PARALLEL_IMPORT_ID to listOf("SOUBEZNY_DOVOZ"),
        COLUMN_MRP_NUMBER to listOf("MRP_CISLO"),
        COLUMN_REGISTRATION_PROCESS to listOf("TYP_REGISTRACE"),
        COLUMN_REGISTRATION_LEGAL_BASIS to listOf("PRAVNI_ZAKLAD_REGISTRACE"),
        COLUMN_MAH_CODE to listOf("DRZITEL"),
        COLUMN_MAH_COUNTRY_CODE to listOf("ZEME_DRZITELE"),
        COLUMN_REGISTRATION_END_DATE to listOf("KONEC_PLATNOSTI_REGISTRACE"),
        COLUMN_REGISTRATION_STATUS to listOf("STAV_REGISTRACE")
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdCancelledRegistration? {
        try {
            // Mandatory attributes
            val registrationNumber = row[headerIndex.getValue(COLUMN_REGISTRATION_NUMBER)].trim()
            if (registrationNumber.isEmpty()) {
                logger.warn { "Missing registration number in row: ${row.joinToString()}" }
                return null
            }

            // Optional attributes helpers
            fun getOptionalString(column: String): String? =
                headerIndex[column]?.let { row.getOrNull(it)?.trim()?.ifBlank { null } }

            fun getOptionalReference(column: String, refMap: Map<String, BaseMpdEntity<*>>): BaseMpdEntity<*>? =
                getOptionalString(column)?.let { refMap[it] }

            // Reference maps
            val adminRoutes = referenceDataProvider.getAdministrationRoutes()
            val dosageForms = referenceDataProvider.getDosageForms()
            val processes = referenceDataProvider.getRegistrationProcesses()
            val statuses = referenceDataProvider.getRegistrationStatuses()
            val organisations = referenceDataProvider.getOrganisations()

            // Composite key for MAH
            val mahCode = getOptionalString(COLUMN_MAH_CODE)
            val mahCountryCode = getOptionalString(COLUMN_MAH_COUNTRY_CODE)
            val marketingAuthorizationHolder = if (mahCode != null && mahCountryCode != null) {
                organisations[mahCode to mahCountryCode]
            } else null

            return MpdCancelledRegistration(
                firstSeen = importedDatasetValidFrom,
                missingSince = null,
                registrationNumber = registrationNumber,
                name = getOptionalString(COLUMN_NAME),
                administrationRoute = getOptionalReference(COLUMN_ADMINISTRATION_ROUTE, adminRoutes) as? MpdAdministrationRoute,
                dosageForm = getOptionalReference(COLUMN_DOSAGE_FORM, dosageForms) as? MpdDosageForm,
                strength = getOptionalString(COLUMN_STRENGTH),
                parallelImportId = getOptionalString(COLUMN_PARALLEL_IMPORT_ID),
                mrpNumber = getOptionalString(COLUMN_MRP_NUMBER),
                registrationProcess = getOptionalReference(COLUMN_REGISTRATION_PROCESS, processes) as? MpdRegistrationProcess,
                registrationLegalBasis = getOptionalString(COLUMN_REGISTRATION_LEGAL_BASIS),
                marketingAuthorizationHolder = marketingAuthorizationHolder,
                registrationEndDate = getOptionalString(COLUMN_REGISTRATION_END_DATE)?.let { parseDate(it) },
                registrationStatus = getOptionalReference(COLUMN_REGISTRATION_STATUS, statuses) as? MpdRegistrationStatus
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse ${getDatasetType().description} row: ${row.joinToString()}" }
            return null
        }
    }

    private fun parseDate(raw: String): LocalDate? {
        return try {
            LocalDate.parse(raw, dateFormatter)
        } catch (e: Exception) {
            logger.warn { "Unable to parse date '$raw'" }
            null
        }
    }
}
