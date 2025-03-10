package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdCompositionFlag
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCompositionFlagRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset
import java.time.LocalDate

@Service
class MpdCompositionFlagProcessor(
    private val compositionFlagRepository: MpdCompositionFlagRepository
) {

    private val logger = KotlinLogging.logger {}

    @Transactional
    fun importData(csvBytes: ByteArray, validFromOfNewDataset: LocalDate, validToOfNewDataset: LocalDate?) {
        val text = csvBytes.toString(Charset.forName("Windows-1250")) // Konverze kódování
        val lines = text.split("\r\n", "\n").drop(1).filter { it.isNotBlank() }

        val currentData = lines.mapNotNull { parseLine(it, validFromOfNewDataset) }
        val newCodes = currentData.map { it.code }.toSet()
        val existingRecords = compositionFlagRepository.findAllByCodeIn(newCodes)
        val updatedRecords = mutableListOf<MpdCompositionFlag>()

        currentData.forEach { row ->
            val existing = existingRecords.find { it.code == row.code }
            if (existing == null) {
                updatedRecords += row
            } else {
                var changed = false

                if (existing.meaning != row.meaning) {
                    logger.info { "Code ${existing.code} meaning changed from '${existing.meaning}' to '${row.meaning}'" }
                    changed = true
                }

                if (existing.validTo != null) {
                    logger.info { "Code ${existing.code} reactivated (validFrom ${row.validFrom})" }
                    changed = true
                }

                if (changed) {
                    updatedRecords += existing.copy(
                        meaning = row.meaning,
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

        compositionFlagRepository.saveAll(updatedRecords)
        logger.info { "Processed ${updatedRecords.size} updates for MpdCompositionFlag." }
    }

    private fun parseLine(line: String, validFromOfNewDataset: LocalDate): MpdCompositionFlag? {
        val cols = line.split(";")
        if (cols.size < 2) return null
        val code = cols[0].trim()
        val meaning = cols[1].trim()
        return MpdCompositionFlag(
            code = code,
            meaning = meaning,
            validFrom = validFromOfNewDataset,
            validTo = null
        )
    }
}
