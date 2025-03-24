package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdOrganisation
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAttributeChangeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdOrganisationRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRecordTemporaryAbsenceRepository
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
        private const val COLUMN_KOD = "ZKR_ORG"
        private const val COLUMN_ZEME = "ZEM"
        private const val COLUMN_NAZEV = "NAZEV"
        private const val COLUMN_VYROBCE = "VYROBCE"
        private const val COLUMN_DRZITEL = "DRZITEL"
    }

    override fun getDatasetType(): MpdDatasetType = MpdDatasetType.MPD_ORGANISATION

    override fun getExpectedColumns(): List<String> = listOf(
        COLUMN_KOD,
        COLUMN_ZEME,
        COLUMN_NAZEV,
        COLUMN_VYROBCE,
        COLUMN_DRZITEL
    )

    override fun mapCsvRowToEntity(
        row: Array<String>,
        headerIndex: Map<String, Int>,
        importedDatasetValidFrom: LocalDate
    ): MpdOrganisation? {
        try {
            // Mandatory attributes
            val code = row[headerIndex.getValue(COLUMN_KOD)].trim()
            val countryCode = row[headerIndex.getValue(COLUMN_ZEME)].trim()
            val country = referenceDataProvider.getCountries()[countryCode]
                ?: throw IllegalArgumentException("Country code $countryCode not found")

            // Optional attributes
            val name = headerIndex[COLUMN_NAZEV]
                ?.let { row.getOrNull(it)?.trim() }
            val isManufacturer = headerIndex[COLUMN_VYROBCE]
                ?.let { row.getOrNull(it)?.trim().equals("V", ignoreCase = true) }
            val isMarketingAuthHolder = headerIndex[COLUMN_DRZITEL]
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
