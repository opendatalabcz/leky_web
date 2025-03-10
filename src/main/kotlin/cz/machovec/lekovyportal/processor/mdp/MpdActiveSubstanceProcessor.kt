package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdActiveSubstance
import cz.machovec.lekovyportal.domain.repository.mpd.MpdActiveSubstanceRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAddictionCategoryRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdActiveSubstanceProcessor(
    private val activeSubstanceRepository: MpdActiveSubstanceRepository,
    private val addictionCategoryRepository: MpdAddictionCategoryRepository
) {

    @Transactional
    fun importData(csvBytes: ByteArray, validFromOfNewDataset: LocalDate, validToOfNewDataset: LocalDate?) {
        val text = csvBytes.toString(Charset.forName("Windows-1250"))
        val lines = text.split("\r\n", "\n").drop(1).filter { it.isNotBlank() }

        val currentData = lines.mapNotNull { parseLine(it, validFromOfNewDataset) }
        val newCodes = currentData.map { it.code }.toSet()
        val existingRecords = activeSubstanceRepository.findAllByCodeIn(newCodes)
        val updatedRecords = mutableListOf<MpdActiveSubstance>()

        currentData.forEach { row ->
            val existing = existingRecords.find { it.code == row.code }
            if (existing == null) {
                updatedRecords += row
            } else {
                var changed = false

                if (existing.nameInn != row.nameInn || existing.nameEn != row.nameEn || existing.name != row.name) {
                    logger.info { "Code ${existing.code} changed: '${existing.nameInn}' -> '${row.nameInn}', '${existing.nameEn}' -> '${row.nameEn}', '${existing.name}' -> '${row.name}'" }
                    changed = true
                }

                if (existing.validTo != null) {
                    logger.info { "Code ${existing.code} reactivated (validFrom ${row.validFrom})" }
                    changed = true
                }

                if (changed) {
                    updatedRecords += existing.copy(
                        nameInn = row.nameInn,
                        nameEn = row.nameEn,
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

        activeSubstanceRepository.saveAll(updatedRecords)
        logger.info { "Processed ${updatedRecords.size} updates for MpdActiveSubstance." }
    }

    private fun parseLine(line: String, validFromOfNewDataset: LocalDate): MpdActiveSubstance? {
        val cols = line.split(";")
        if (cols.size < 5) return null
        val code = cols[0].trim()
        val nameInn = cols[1].trim()
        val nameEn = cols[2].trim()
        val name = cols[3].trim()
        val addictionCategoryCode = cols[4].takeIf { it.isNotEmpty() }?.trim()

        val addictionCategory = addictionCategoryCode?.let {
            addictionCategoryRepository.findByCode(it)
        }

        return MpdActiveSubstance(
            code = code,
            nameInn = nameInn,
            nameEn = nameEn,
            name = name,
            addictionCategory = addictionCategory,
            validFrom = validFromOfNewDataset,
            validTo = null
        )
    }
}
