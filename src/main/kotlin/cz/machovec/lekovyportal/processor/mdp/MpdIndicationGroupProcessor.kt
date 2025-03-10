package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdIndicationGroup
import cz.machovec.lekovyportal.domain.repository.mpd.MpdIndicationGroupRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdIndicationGroupProcessor(
    private val indicationGroupRepository: MpdIndicationGroupRepository
) {

    @Transactional
    fun importData(csvBytes: ByteArray, validFromOfNewDataset: LocalDate, validToOfNewDataset: LocalDate?) {
        val text = csvBytes.toString(Charset.forName("windows-1250"))
        val lines = text.split("\r\n", "\n").drop(1).filter { it.isNotBlank() }
        val currentData = lines.mapNotNull { parseLine(it, validFromOfNewDataset) }
        val newCodes = currentData.map { it.code }.toSet()
        val existingRecords = indicationGroupRepository.findAllByCodeIn(newCodes)
        val updatedRecords = mutableListOf<MpdIndicationGroup>()

        currentData.forEach { row ->
            val existing = existingRecords.find { it.code == row.code }
            if (existing == null) {
                updatedRecords += row
            } else {
                var changed = false

                if (existing.name != row.name) {
                    logger.info { "Code ${existing.code} name changed from '${existing.name}' to '${row.name}'" }
                    changed = true
                }

                if (existing.validTo != null) {
                    logger.info { "Code ${existing.code} reactivated (validFrom ${row.validFrom})" }
                    changed = true
                }

                if (changed) {
                    updatedRecords += existing.copy(
                        name = row.name,
                        validTo = null,
                        validFrom = row.validFrom
                    )
                }
            }
        }

        val missing = existingRecords.filter { !newCodes.contains(it.code) && it.validTo == null }
        missing.forEach {
            updatedRecords += it.copy(validTo = validToOfNewDataset ?: validFromOfNewDataset)
            logger.info { "Code ${it.code} marked invalid from ${validToOfNewDataset ?: validFromOfNewDataset}" }
        }

        indicationGroupRepository.saveAll(updatedRecords)
        logger.info { "Processed ${updatedRecords.size} updates." }
    }

    private fun parseLine(line: String, validFromOfNewDataset: LocalDate): MpdIndicationGroup? {
        val cols = line.split(";")
        if (cols.size < 2) return null
        val code = cols[0].trim()
        val name = cols[1].trim().ifEmpty { null } ?: return null
        return MpdIndicationGroup(
            code = code,
            name = name,
            validFrom = validFromOfNewDataset,
            validTo = null
        )
    }
}
