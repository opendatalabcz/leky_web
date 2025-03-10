package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdAtcGroup
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAtcGroupRepository
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.nio.charset.Charset
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdAtcGroupProcessor(
    private val atcGroupRepository: MpdAtcGroupRepository
) {

    @Transactional
    fun importData(csvBytes: ByteArray, validFromOfNewDataset: LocalDate, validToOfNewDataset: LocalDate?) {
        val text = csvBytes.toString(Charset.forName("Windows-1250"))
        val lines = text.split("\r\n", "\n").drop(1).filter { it.isNotBlank() }

        val currentData = lines.mapNotNull { parseLine(it, validFromOfNewDataset) }
        val newCodes = currentData.map { it.code }.toSet()
        val existingRecords = atcGroupRepository.findAllByCodeIn(newCodes)
        val updatedRecords = mutableListOf<MpdAtcGroup>()

        currentData.forEach { row ->
            val existing = existingRecords.find { it.code == row.code }
            if (existing == null) {
                updatedRecords += row
            } else {
                var changed = false

                if (existing.name != row.name || existing.nameEn != row.nameEn || existing.type != row.type) {
                    logger.info { "Code ${existing.code} changed: Name '${existing.name}' → '${row.name}', NameEN '${existing.nameEn}' → '${row.nameEn}', Type '${existing.type}' → '${row.type}'" }
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
                        type = row.type,
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

        atcGroupRepository.saveAll(updatedRecords)
        logger.info { "Processed ${updatedRecords.size} updates for MpdAtcGroup." }
    }

    private fun parseLine(line: String, validFromOfNewDataset: LocalDate): MpdAtcGroup? {
        val cols = line.split(";")
        if (cols.size < 4) return null
        val code = cols[0].trim()
        val type = cols[1].trim().firstOrNull() ?: return null
        val name = cols[2].trim()
        val nameEn = cols[3].trim()
        return MpdAtcGroup(
            code = code,
            type = type,
            name = name,
            nameEn = nameEn,
            validFrom = validFromOfNewDataset,
            validTo = null
        )
    }
}
