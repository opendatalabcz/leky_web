package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdAdministrationRoute
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAdministrationRouteRepository
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.nio.charset.Charset
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdAdministrationRouteProcessor(
    private val administrationRouteRepository: MpdAdministrationRouteRepository
) {

    @Transactional
    fun importData(csvBytes: ByteArray, validFromOfNewDataset: LocalDate, validToOfNewDataset: LocalDate?) {
        val text = csvBytes.toString(Charset.forName("Windows-1250"))
        val lines = text.split("\r\n", "\n").drop(1).filter { it.isNotBlank() }
        val currentData = lines.mapNotNull { parseLine(it, validFromOfNewDataset) }
        val newCodes = currentData.map { it.code }.toSet()
        val existingRecords = administrationRouteRepository.findAllByCodeIn(newCodes)
        val updatedRecords = mutableListOf<MpdAdministrationRoute>()

        currentData.forEach { row ->
            val existing = existingRecords.find { it.code == row.code }
            if (existing == null) {
                updatedRecords += row
            } else {
                var changed = false

                if (existing.name != row.name || existing.nameEn != row.nameEn || existing.nameLat != row.nameLat || existing.edqmCode != row.edqmCode) {
                    logger.info { "Code ${existing.code} changed from '${existing.name}' to '${row.name}'" }
                    changed = true
                }

                if (existing.validTo != null) {
                    logger.info { "Code ${existing.code} reactivated (validFrom ${row.validFrom})" }
                    changed = true
                }

                if (changed) {
                    updatedRecords += existing.copy(
                        name = row.name,
                        nameEn = row.nameEn,
                        nameLat = row.nameLat,
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
            logger.info { "Code ${it.code} marked invalid from $validFromOfNewDataset" }
        }

        administrationRouteRepository.saveAll(updatedRecords)
        logger.info { "Processed ${updatedRecords.size} updates for MpdAdministrationRoute." }
    }

    private fun parseLine(line: String, validFromOfNewDataset: LocalDate): MpdAdministrationRoute? {
        val cols = line.split(";")
        if (cols.size < 5) return null
        val code = cols[0].trim()
        val name = cols[1].trim()
        val nameEn = cols[2].trim()
        val nameLat = cols[3].trim()
        val edqmCode = cols[4].takeIf { it.isNotEmpty() }?.trim()?.toLongOrNull()
        return MpdAdministrationRoute(
            code = code,
            name = name,
            nameEn = nameEn,
            nameLat = nameLat,
            edqmCode = edqmCode,
            validFrom = validFromOfNewDataset,
            validTo = null
        )
    }
}
