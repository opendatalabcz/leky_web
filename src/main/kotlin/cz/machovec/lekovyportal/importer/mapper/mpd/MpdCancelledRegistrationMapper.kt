package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdCancelledRegistration
import cz.machovec.lekovyportal.importer.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
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
    private val validFrom: LocalDate
) : BaseSimpleRowMapper<MpdCancelledRegistrationColumn, MpdCancelledRegistration>() {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override fun map(row: CsvRow<MpdCancelledRegistrationColumn>, rawLine: String): RowMappingResult<MpdCancelledRegistration> {

        val registrationNumber = row[MpdCancelledRegistrationColumn.REGISTRATION_NUMBER].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdCancelledRegistrationColumn.REGISTRATION_NUMBER.name, rawLine)
            )

        val entity = MpdCancelledRegistration(
            firstSeen = validFrom,
            missingSince = null,
            registrationNumber = registrationNumber,
            name = row[MpdCancelledRegistrationColumn.NAME].safeTrim(),
            administrationRouteCode = row[MpdCancelledRegistrationColumn.ADMINISTRATION_ROUTE].safeTrim(),
            dosageFormCode = row[MpdCancelledRegistrationColumn.DOSAGE_FORM].safeTrim(),
            strength = row[MpdCancelledRegistrationColumn.STRENGTH].safeTrim(),
            parallelImportId = row[MpdCancelledRegistrationColumn.PARALLEL_IMPORT_ID].safeTrim(),
            mrpNumber = row[MpdCancelledRegistrationColumn.MRP_NUMBER].safeTrim(),
            registrationProcessCode = row[MpdCancelledRegistrationColumn.REGISTRATION_PROCESS].safeTrim(),
            registrationLegalBasis = row[MpdCancelledRegistrationColumn.REGISTRATION_LEGAL_BASIS].safeTrim(),
            mahCode = row[MpdCancelledRegistrationColumn.MAH_CODE].safeTrim(),
            mahCountryCode = row[MpdCancelledRegistrationColumn.MAH_COUNTRY_CODE].safeTrim(),
            registrationEndDate = parseDate(row[MpdCancelledRegistrationColumn.REGISTRATION_END_DATE]),
            registrationStatusCode = row[MpdCancelledRegistrationColumn.REGISTRATION_STATUS].safeTrim()
        )

        return RowMappingResult.Success(entity)
    }

    private fun parseDate(raw: String?): LocalDate? {
        if (raw.isNullOrBlank()) return null
        return try {
            LocalDate.parse(raw, dateFormatter)
        } catch (e: Exception) {
            null
        }
    }
}
