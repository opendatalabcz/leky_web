package cz.machovec.lekovyportal.importer.mapper.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdOrganisation
import cz.machovec.lekovyportal.importer.columns.mpd.MpdOrganisationColumn
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.RowMapper
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider
import mu.KotlinLogging
import java.time.LocalDate

class MpdOrganisationRowMapper(
    private val validFrom: LocalDate,
    private val referenceDataProvider: MpdReferenceDataProvider
) : RowMapper<MpdOrganisationColumn, MpdOrganisation> {

    private val logger = KotlinLogging.logger {}

    override fun map(row: CsvRow<MpdOrganisationColumn>): MpdOrganisation? {
        val code = row[MpdOrganisationColumn.CODE]?.trim().takeIf { !it.isNullOrBlank() } ?: return null

        val countryCode = row[MpdOrganisationColumn.COUNTRY]?.trim()
        val country = countryCode?.let { referenceDataProvider.getCountries()[it] }
        if (country == null) {
            logger.warn { "Unknown country code '$countryCode' – skipping row." }
            return null
        }

        val name = row[MpdOrganisationColumn.NAME]?.trim()
        val isManufacturer = row[MpdOrganisationColumn.IS_MANUFACTURER]
            ?.trim()
            ?.equals("V", ignoreCase = true)

        val isMarketingAuthHolder = row[MpdOrganisationColumn.IS_MARKETING_AUTH_HOLDER]
            ?.trim()
            ?.equals("D", ignoreCase = true)

        return MpdOrganisation(
            firstSeen = validFrom,
            missingSince = null,
            code = code,
            country = country,
            name = name,
            isManufacturer = isManufacturer,
            isMarketingAuthorizationHolder = isMarketingAuthHolder
        )
    }
}
