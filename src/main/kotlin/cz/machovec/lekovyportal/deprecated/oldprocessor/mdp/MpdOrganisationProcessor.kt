package cz.machovec.lekovyportal.deprecated.oldprocessor.mdp

import cz.machovec.lekovyportal.core.domain.mpd.MpdDatasetType
import cz.machovec.lekovyportal.core.domain.mpd.MpdOrganisation
import cz.machovec.lekovyportal.core.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdOrganisationRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdRecordTemporaryAbsenceRepository
import cz.machovec.lekovyportal.processor.processing.mpd.MpdReferenceDataProvider
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdOrganisationProcessor(
    organisationRepository: MpdOrganisationRepository,
    attributeChangeRepository: MpdAttributeChangeRepository,
    temporaryAbsenceRepository: MpdRecordTemporaryAbsenceRepository,
    private val referenceDataProvider: MpdReferenceDataProvider
) : BaseMpdProcessor<MpdOrganisation>(
    organisationRepository,
    attributeChangeRepository,
    temporaryAbsenceRepository
) {
    companion object {
        private const val COLUMN_CODE = "code"
        private const val COLUMN_COUNTRY = "country"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_IS_MANUFACTURER = "isManufacturer"
        private const val COLUMN_IS_MARKETING_AUTH_HOLDER = "isMarketingAuthorizationHolder"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_ORGANISATION

    override fun getExpectedColumnsMap(): Map<String, List<String>> = mapOf(
        COLUMN_CODE to listOf("ZKR_ORG"),
        COLUMN_COUNTRY to listOf("ZEM"),
        COLUMN_NAME to listOf("NAZEV"),
        COLUMN_IS_MANUFACTURER to listOf("VYROBCE"),
        COLUMN_IS_MARKETING_AUTH_HOLDER to listOf("DRZITEL")
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdOrganisation? {
        try {
            // Mandatory attributes
            val code = row[headerIndex.getValue(COLUMN_CODE)].trim()
            val countryCode = row[headerIndex.getValue(COLUMN_COUNTRY)].trim()
            val country = referenceDataProvider.getCountries()[countryCode]
                ?: return null.also {
                    logger.warn { "Unknown country code '$countryCode' – skipping row." }
                }

            // Optional attributes
            val name = headerIndex[COLUMN_NAME]?.let { row.getOrNull(it)?.trim() }
            val isManufacturer = headerIndex[COLUMN_IS_MANUFACTURER]
                ?.let { row.getOrNull(it)?.trim().equals("V", ignoreCase = true) }
            val isMarketingAuthHolder = headerIndex[COLUMN_IS_MARKETING_AUTH_HOLDER]
                ?.let { row.getOrNull(it)?.trim().equals("D", ignoreCase = true) }

            return MpdOrganisation(
                firstSeen = importedDatasetValidFrom,
                missingSince = null,
                code = code,
                country = country,
                name = name,
                isManufacturer = isManufacturer,
                isMarketingAuthorizationHolder = isMarketingAuthHolder,
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse ${getDatasetType().description} row: ${row.joinToString()}" }
            return null
        }
    }
}
