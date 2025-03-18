package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdSubstance
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAddictionCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDopingCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdGovernmentRegulationCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdSourceRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdSubstanceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdSubstanceProcessor(
    private val substanceRepository: MpdSubstanceRepository,
    private val sourceRepository: MpdSourceRepository,
    private val addictionCategoryRepository: MpdAddictionCategoryRepository,
    private val dopingCategoryRepository: MpdDopingCategoryRepository,
    private val governmentRegulationCategoryRepository: MpdGovernmentRegulationCategoryRepository
) {

    @Transactional
    fun importData(csvBytes: ByteArray, validFromOfNewDataset: LocalDate, validToOfNewDataset: LocalDate?) {
        val text = csvBytes.toString(Charset.forName("Windows-1250"))
        val lines = text.split("\r\n", "\n").drop(1).filter { it.isNotBlank() }

        val currentData = lines.mapNotNull { parseLine(it, validFromOfNewDataset) }
        val newCodes = currentData.map { it.code }.toSet()
        val existingRecords = substanceRepository.findAllByCodeIn(newCodes)
        val updatedRecords = mutableListOf<MpdSubstance>()

        currentData.forEach { row ->
            val existing = existingRecords.find { it.code == row.code }
            if (existing == null) {
                updatedRecords += row
            } else {
                var changed = false

                if (existing.nameInn != row.nameInn || existing.nameEn != row.nameEn || existing.name != row.name ||
                    existing.source.id != row.source.id ||
                    existing.addictionCategory?.id != row.addictionCategory?.id ||
                    existing.dopingCategory?.id != row.dopingCategory?.id ||
                    existing.governmentRegulationCategory?.id != row.governmentRegulationCategory?.id
                ) {
                    logger.info { "Code ${existing.code} has changed. Updating record." }
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
                        source = row.source,
                        addictionCategory = row.addictionCategory,
                        dopingCategory = row.dopingCategory,
                        governmentRegulationCategory = row.governmentRegulationCategory,
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

        substanceRepository.saveAll(updatedRecords)
        logger.info { "Processed ${updatedRecords.size} updates for MpdSubstance." }
    }

    private fun parseLine(line: String, validFromOfNewDataset: LocalDate): MpdSubstance? {
        val cols = line.split(";")
        if (cols.size < 8) return null
        val code = cols[0].trim()
        val sourceCode = cols[1].trim()
        val nameInn = cols[2].trim()
        val nameEn = cols[3].trim()
        val name = cols[4].trim()
        val addictionCategoryCode = cols[5].takeIf { it.isNotEmpty() }?.trim()
        val dopingCategoryCode = cols[6].takeIf { it.isNotEmpty() }?.trim()
        val governmentRegulationCategoryCode = cols[7].takeIf { it.isNotEmpty() }?.trim()

        val source = sourceRepository.findByCode(sourceCode)
            ?: return null.also { logger.warn { "Source with code $sourceCode not found. Skipping row." } }

        val addictionCategory = addictionCategoryCode?.let {
            addictionCategoryRepository.findByCode(it)
        }

        val dopingCategory = dopingCategoryCode?.let {
            dopingCategoryRepository.findByCode(it)
        }

        val governmentRegulationCategory = governmentRegulationCategoryCode?.let {
            governmentRegulationCategoryRepository.findByCode(it)
        }

        return MpdSubstance(
            code = code,
            source = source,
            nameInn = nameInn,
            nameEn = nameEn,
            name = name,
            addictionCategory = addictionCategory,
            dopingCategory = dopingCategory,
            governmentRegulationCategory = governmentRegulationCategory,
            validFrom = validFromOfNewDataset,
            validTo = null
        )
    }
}
