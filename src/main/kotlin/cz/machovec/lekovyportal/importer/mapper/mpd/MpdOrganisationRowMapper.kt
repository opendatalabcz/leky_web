package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdOrganisation
import cz.machovec.lekovyportal.importer.mapper.BaseRefRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
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

    override fun map(row: CsvRow<MpdOrganisationColumn>): MpdOrganisation? {
        val code = row[MpdOrganisationColumn.CODE].safeTrim() ?: return null

        val country = row[MpdOrganisationColumn.COUNTRY]
            .safeTrim()
            ?.let { ref.getCountries()[it] }
            ?: return null

        return MpdOrganisation(
            firstSeen    = validFrom,
            missingSince = null,
            code         = code,
            country      = country,
            name         = row[MpdOrganisationColumn.NAME].safeTrim(),
            isManufacturer               = row[MpdOrganisationColumn.IS_MANUFACTURER]
                .safeTrim()
                ?.equals("V", true),
            isMarketingAuthorizationHolder = row[MpdOrganisationColumn.IS_MARKETING_AUTH_HOLDER]
                .safeTrim()
                ?.equals("D", true)
        )
    }
}
