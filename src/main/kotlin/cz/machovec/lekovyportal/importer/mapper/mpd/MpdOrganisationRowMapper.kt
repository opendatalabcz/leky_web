package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdOrganisation
import cz.machovec.lekovyportal.importer.mapper.BaseRefRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider
import java.time.LocalDate

enum class MpdOrganisationColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    CODE(listOf("ZKR_ORG")),
    COUNTRY(listOf("ZEM")),
    NAME(listOf("NAZEV"), required = false),
    IS_MANUFACTURER(listOf("VYROBCE"), required = false),
    IS_MARKETING_AUTH_HOLDER(listOf("DRZITEL"), required = false);
}

class MpdOrganisationRowMapper(
    private val validFrom: LocalDate,
    refProvider: MpdReferenceDataProvider
) : BaseRefRowMapper<MpdOrganisationColumn, MpdOrganisation>(refProvider) {

    override fun map(row: CsvRow<MpdOrganisationColumn>, rawLine: String): RowMappingResult<MpdOrganisation> {

        /* ---------- mandatory attributes ---------- */
        val code = row[MpdOrganisationColumn.CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdOrganisationColumn.CODE.name, rawLine)
            )

        val country = row[MpdOrganisationColumn.COUNTRY]
            .safeTrim()
            ?.let { ref.getCountries()[it] }
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.UNKNOWN_REFERENCE, MpdOrganisationColumn.COUNTRY.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val name = row[MpdOrganisationColumn.NAME].safeTrim()
        val isManufacturer = row[MpdOrganisationColumn.IS_MANUFACTURER]
            .safeTrim()
            ?.equals("V", true)
        val isMAHolder = row[MpdOrganisationColumn.IS_MARKETING_AUTH_HOLDER]
            .safeTrim()
            ?.equals("D", true)

        /* ---------- entity construction ---------- */
        val entity = MpdOrganisation(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            country = country,
            name = name,
            isManufacturer = isManufacturer,
            isMarketingAuthorizationHolder = isMAHolder
        )

        return RowMappingResult.Success(entity)
    }
}
