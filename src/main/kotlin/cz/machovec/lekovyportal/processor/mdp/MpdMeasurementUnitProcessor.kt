package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdMeasurementUnit
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMeasurementUnitRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdMeasurementUnitProcessor(
    private val measurementUnitRepository: MpdMeasurementUnitRepository
) {

    @Transactional
    fun importData(csvBytes: ByteArray, validFromOfNewDataset: LocalDate, validToOfNewDataset: LocalDate?) {
        val text = csvBytes.toString(Charset.forName("Windows-1250"))
        val lines = text.split("\r\n", "\n").drop(1).filter { it.isNotBlank() }
        val currentData = lines.mapNotNull { parseLine(it, validFromOfNewDataset) }
        val newCodes = currentData.map { it.code }.toSet()
        val existingRecords = measurementUnitRepository.findAllByCodeIn(newCodes)
        val updatedRecords = mutableListOf<MpdMeasurementUnit>()

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
            updatedRecords += it.copy(validTo = validFromOfNewDataset)
            logger.info { "Code ${it.code} marked invalid from $validFromOfNewDataset" }
        }

        measurementUnitRepository.saveAll(updatedRecords)
        logger.info { "Processed ${updatedRecords.size} updates for MpdMeasurementUnit." }
    }

    private fun parseLine(line: String, validFromOfNewDataset: LocalDate): MpdMeasurementUnit? {
        val cols = line.split(";")
        if (cols.size < 2) return null
        val code = cols[0].trim()
        val name = cols[1].trim()
        return MpdMeasurementUnit(
            code = code,
            name = name,
            validFrom = validFromOfNewDataset,
            validTo = null
        )
    }
}
