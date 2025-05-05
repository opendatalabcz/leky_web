package cz.machovec.lekovyportal.processor.mapper.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdCancelledRegistration
import cz.machovec.lekovyportal.processor.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.processor.mapper.ColumnAlias
import cz.machovec.lekovyportal.processor.mapper.CsvRow
import cz.machovec.lekovyportal.processor.mapper.FailureReason
import cz.machovec.lekovyportal.processor.mapper.RowFailure
import cz.machovec.lekovyportal.processor.mapper.RowMappingResult
import cz.machovec.lekovyportal.processor.processing.mpd.MpdReferenceDataProvider
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class MpdCancelledRegistrationColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    NAME(listOf("NAZEV"), required = false),
    ADMINISTRATION_ROUTE(listOf("CESTA"), required = false),
    DOSAGE_FORM(listOf("FORMA"), required = false),
    STRENGTH(listOf("SILA"), required = false),
    REGISTRATION_NUMBER(listOf("REGISTRACNI_CISLO")),
    PARALLEL_IMPORT_ID(listOf("SOUBEZNY_DOVOZ"), required = false),
    MRP_NUMBER(listOf("MRP_CISLO"), required = false),
    REGISTRATION_PROCESS(listOf("TYP_REGISTRACE"), required = false),
    REGISTRATION_LEGAL_BASIS(listOf("PRAVNI_ZAKLAD_REGISTRACE"), required = false),
    MAH_CODE(listOf("DRZITEL"), required = false),
    MAH_COUNTRY_CODE(listOf("ZEME_DRZITELE"), required = false),
    REGISTRATION_END_DATE(listOf("KONEC_PLATNOSTI_REGISTRACE"), required = false),
    REGISTRATION_STATUS(listOf("STAV_REGISTRACE"), required = false);
}

class MpdCancelledRegistrationRowMapper(
    private val validFrom: LocalDate,
    private val refProvider: MpdReferenceDataProvider
) : BaseSimpleRowMapper<MpdCancelledRegistrationColumn, MpdCancelledRegistration>() {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override fun map(row: CsvRow<MpdCancelledRegistrationColumn>, rawLine: String): RowMappingResult<MpdCancelledRegistration> {

        /* ---------- mandatory attributes ---------- */
        val registrationNumber = row[MpdCancelledRegistrationColumn.REGISTRATION_NUMBER].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdCancelledRegistrationColumn.REGISTRATION_NUMBER.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val name = row[MpdCancelledRegistrationColumn.NAME].safeTrim()

        val administrationRoute = row[MpdCancelledRegistrationColumn.ADMINISTRATION_ROUTE]
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { refProvider.getAdministrationRoutes()[it] }

        val dosageForm = row[MpdCancelledRegistrationColumn.DOSAGE_FORM]
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { refProvider.getDosageForms()[it] }

        val strength = row[MpdCancelledRegistrationColumn.STRENGTH].safeTrim()

        val parallelImportId = row[MpdCancelledRegistrationColumn.PARALLEL_IMPORT_ID].safeTrim()
        val mrpNumber = row[MpdCancelledRegistrationColumn.MRP_NUMBER].safeTrim()

        val registrationProcess = row[MpdCancelledRegistrationColumn.REGISTRATION_PROCESS]
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { refProvider.getRegistrationProcesses()[it] }

        val registrationLegalBasis = row[MpdCancelledRegistrationColumn.REGISTRATION_LEGAL_BASIS].safeTrim()

        val mahCode = row[MpdCancelledRegistrationColumn.MAH_CODE].safeTrim()
        val mahCountryCode = row[MpdCancelledRegistrationColumn.MAH_COUNTRY_CODE].safeTrim()
        val marketingAuthorizationHolder = if (!mahCode.isNullOrBlank() && !mahCountryCode.isNullOrBlank()) {
            refProvider.getOrganisations()[mahCode to mahCountryCode]
        } else null

        val registrationEndDate = row[MpdCancelledRegistrationColumn.REGISTRATION_END_DATE]
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { parseDate(it) }

        val registrationStatus = row[MpdCancelledRegistrationColumn.REGISTRATION_STATUS]
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { refProvider.getRegistrationStatuses()[it] }

        /* ---------- entity construction ---------- */
        val entity = MpdCancelledRegistration(
            firstSeen = validFrom,
            missingSince = null,
            registrationNumber = registrationNumber,
            name = name,
            administrationRoute = administrationRoute,
            dosageForm = dosageForm,
            strength = strength,
            parallelImportId = parallelImportId,
            mrpNumber = mrpNumber,
            registrationProcess = registrationProcess,
            registrationLegalBasis = registrationLegalBasis,
            marketingAuthorizationHolder = marketingAuthorizationHolder,
            registrationEndDate = registrationEndDate,
            registrationStatus = registrationStatus
        )

        return RowMappingResult.Success(entity)
    }

    private fun parseDate(value: String): LocalDate? {
        return try {
            LocalDate.parse(value, dateFormatter)
        } catch (e: Exception) {
            null
        }
    }
}
