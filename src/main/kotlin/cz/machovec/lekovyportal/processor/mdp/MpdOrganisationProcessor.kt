package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdCountry
import cz.machovec.lekovyportal.domain.entity.mpd.MpdOrganisation
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCountryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdOrganisationRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdOrganisationProcessor(
    private val organisationRepository: MpdOrganisationRepository,
    private val countryRepository: MpdCountryRepository
) {

    @Transactional
    fun importData(csvBytes: ByteArray, validFromOfNewDataset: LocalDate, validToOfNewDataset: LocalDate?) {
        val text = csvBytes.toString(Charset.forName("Windows-1250")) // Správná konverze kódování
        val lines = text.split("\r\n", "\n").drop(1).filter { it.isNotBlank() }

        val uniqueCountryCodes = lines.map { it.split(";")[1].trim() }.toSet()
        val existingCountries = countryRepository.findAllByCodeIn(uniqueCountryCodes)
            .associateBy { it.code }
        val currentData = lines.mapNotNull { parseLine(it, validFromOfNewDataset, existingCountries) }

        val resolvedData = currentData.mapNotNull { row ->
            val country = existingCountries[row.country.code] ?: run {
                logger.warn("Skipping organisation ${row.code} - unknown country ${row.country.code}")
                return@mapNotNull null
            }
            row.copy(country = country)
        }

        val newKeys = resolvedData.map { it.code to it.country }.toSet()
        val existingRecords = organisationRepository.findAllByCodeInAndCountry(
            resolvedData.map { it.code }.toSet(),
            resolvedData.firstOrNull()?.country ?: return
        )
        val updatedRecords = mutableListOf<MpdOrganisation>()

        resolvedData.forEach { row ->
            val existing = existingRecords.find { it.code == row.code && it.country == row.country }
            if (existing == null) {
                updatedRecords += row
            } else {
                var changed = false

                if (existing.name != row.name) {
                    logger.info { "Organisation ${existing.code} name changed from '${existing.name}' to '${row.name}'" }
                    changed = true
                }
                if (existing.isManufacturer != row.isManufacturer) {
                    logger.info { "Organisation ${existing.code} manufacturer flag changed from '${existing.isManufacturer}' to '${row.isManufacturer}'" }
                    changed = true
                }
                if (existing.isMarketingAuthorizationHolder != row.isMarketingAuthorizationHolder) {
                    logger.info { "Organisation ${existing.code} MAH flag changed from '${existing.isMarketingAuthorizationHolder}' to '${row.isMarketingAuthorizationHolder}'" }
                    changed = true
                }
                if (existing.validTo != null) {
                    logger.info { "Organisation ${existing.code} reactivated (validFrom ${row.validFrom})" }
                    changed = true
                }

                if (changed) {
                    updatedRecords += existing.copy(
                        name = row.name,
                        isManufacturer = row.isManufacturer,
                        isMarketingAuthorizationHolder = row.isMarketingAuthorizationHolder,
                        validTo = null,
                        validFrom = row.validFrom
                    )
                }
            }
        }

        val missing = existingRecords.filter { !newKeys.contains(it.code to it.country) && it.validTo == null }
        missing.forEach {
            updatedRecords += it.copy(validTo = validFromOfNewDataset)
            logger.info { "Organisation ${it.code} marked invalid from $validFromOfNewDataset" }
        }

        organisationRepository.saveAll(updatedRecords)
        logger.info { "Processed ${updatedRecords.size} updates for MpdOrganisation." }
    }

    private fun parseLine(
        line: String,
        validFromOfNewDataset: LocalDate,
        existingCountries: Map<String, MpdCountry>
    ): MpdOrganisation? {
        val cols = line.split(";")
        if (cols.size < 5) return null
        val code = cols[0].trim()
        val countryCode = cols[1].trim()
        val name = cols[2].trim()
        val isManufacturer = cols[3] == "V"
        val isMarketingAuthorizationHolder = cols[4] == "D"

        val country = existingCountries[countryCode] ?: run {
            logger.warn("Skipping organisation $code - unknown country $countryCode")
            return null
        }

        return MpdOrganisation(
            code = code,
            country = country,
            name = name,
            isManufacturer = isManufacturer,
            isMarketingAuthorizationHolder = isMarketingAuthorizationHolder,
            validFrom = validFromOfNewDataset,
            validTo = null
        )
    }
}
