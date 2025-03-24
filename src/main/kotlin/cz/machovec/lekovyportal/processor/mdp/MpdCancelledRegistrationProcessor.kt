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
        private const val COLUMN_NAME = "NAZEV"
        private const val COLUMN_ADMINISTRATION_ROUTE = "CESTA"
        private const val COLUMN_DOSAGE_FORM = "FORMA"
        private const val COLUMN_STRENGTH = "SILA"
        private const val COLUMN_REGISTRATION_NUMBER = "REGISTRACNI_CISLO"
        private const val COLUMN_PARALLEL_IMPORT_ID = "SOUBEZNY_DOVOZ"
        private const val COLUMN_MRP_NUMBER = "MRP_CISLO"
        private const val COLUMN_REGISTRATION_PROCESS = "TYO_REGISTRACE"
        private const val COLUMN_REGISTRATION_LEGAL_BASIS = "PRAVNI_ZAKLAD_REGISTRACE"
        private const val COLUMN_MAH_CODE = "DRZITEL"
        private const val COLUMN_MAH_COUNTRY_CODE = "ZEME_DRZITELE"
        private const val COLUMN_REGISTRATION_END_DATE = "KONEC_PLATNOSTI_REGISTRACE"
        private const val COLUMN_REGISTRATION_STATUS = "STAV_REGISTRACE"
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_CANCELLED_REGISTRATION

    override fun getExpectedColumns(): List<String> = listOf(
        COLUMN_NAME,
        COLUMN_ADMINISTRATION_ROUTE,
        COLUMN_DOSAGE_FORM,
        COLUMN_STRENGTH,
        COLUMN_REGISTRATION_NUMBER,
        COLUMN_PARALLEL_IMPORT_ID,
        COLUMN_MRP_NUMBER,
        COLUMN_REGISTRATION_PROCESS,
        COLUMN_REGISTRATION_LEGAL_BASIS,
        COLUMN_MAH_CODE,
        COLUMN_MAH_COUNTRY_CODE,
        COLUMN_REGISTRATION_END_DATE,
        COLUMN_REGISTRATION_STATUS
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