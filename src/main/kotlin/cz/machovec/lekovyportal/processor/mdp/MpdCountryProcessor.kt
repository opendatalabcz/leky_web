package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdCountry
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCountryRepository
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.nio.charset.Charset
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdCountryProcessor(
    private val countryRepository: MpdCountryRepository
) {

    @Transactional
    fun importData(csvBytes: ByteArray, validFromOfNewDataset: LocalDate, validToOfNewDataset: LocalDate?) {
        val text = csvBytes.toString(Charset.forName("Windows-1250")) // Správné dekódování znaků
        val lines = text.split("\r\n", "\n").drop(1).filter { it.isNotBlank() }
        val currentData = lines.mapNotNull { parseLine(it, validFromOfNewDataset) }
        val newCodes = currentData.map { it.code }.toSet()
        val existingRecords = countryRepository.findAllByCodeIn(newCodes)
        val updatedRecords = mutableListOf<MpdCountry>()

        currentData.forEach { row ->
            val existing = existingRecords.find { it.code == row.code }
            if (existing == null) {
                updatedRecords += row
            } else {
                var changed = false

                if (existing.name != row.name || existing.nameEn != row.nameEn || existing.edqmCode != row.edqmCode) {
                    logger.info { "Country ${existing.code} updated: '${existing.name}' -> '${row.name}', '${existing.nameEn}' -> '${row.nameEn}', '${existing.edqmCode}' -> '${row.edqmCode}'" }
                    changed = true
                }

                if (existing.validTo != null) {
                    logger.info { "Country ${existing.code} reactivated (validFrom ${row.validFrom})" }
                    changed = true
                }

                if (changed) {
                    updatedRecords += existing.copy(
                        name = row.name,
                        nameEn = row.nameEn,
                        edqmCode = row.edqmCode,
                        validTo = null,
                        validFrom = row.validFrom
                    )
                }
            }
        }

        val missing = existingRecords.filter { !newCodes.contains(it.code) && it.validTo == null }
        missing.forEach {
            updatedRecords += it.copy(validTo = validFromOfNewDataset)
            logger.info { "Country ${it.code} marked invalid from $validFromOfNewDataset" }
        }

        countryRepository.saveAll(updatedRecords)
        logger.info { "Processed ${updatedRecords.size} updates for MpdCountry." }
    }

    private fun parseLine(line: String, validFromOfNewDataset: LocalDate): MpdCountry? {
        val cols = line.split(";")
        if (cols.size < 4) return null
        val code = cols[0].trim()
        val name = cols[1].trim()
        val nameEn = cols[2].trim()
        val edqmCode = cols[3].trim().takeIf { it.isNotEmpty() }

        return MpdCountry(
            code = code,
            name = name,
            nameEn = nameEn,
            edqmCode = edqmCode,
            validFrom = validFromOfNewDataset,
            validTo = null
        )
    }
}
