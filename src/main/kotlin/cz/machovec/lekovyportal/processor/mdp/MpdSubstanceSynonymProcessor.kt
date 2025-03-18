package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdSubstanceSynonym
import cz.machovec.lekovyportal.domain.repository.mpd.MpdSourceRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdSubstanceRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdSubstanceSynonymRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdSubstanceSynonymProcessor(
    private val substanceSynonymRepository: MpdSubstanceSynonymRepository,
    private val substanceRepository: MpdSubstanceRepository,
    private val sourceRepository: MpdSourceRepository
) {

    @Transactional
    fun importData(csvBytes: ByteArray, validFromOfNewDataset: LocalDate) {
        val text = csvBytes.toString(Charset.forName("Windows-1250"))
        val lines = text.split("\r\n", "\n").drop(1).filter { it.isNotBlank() }

        val currentData = lines.mapNotNull { parseLine(it, validFromOfNewDataset) }
        val newEntries = currentData.toMutableList() // TODO: Měl bych zkontrolovat správnost toho porovnávání starých a nových až budu vědět co přesně byl PK
        val existingRecords = substanceSynonymRepository.findAll()
        val updatedRecords = mutableListOf<MpdSubstanceSynonym>()

        currentData.forEach { row ->
            val existing = existingRecords.find { it.substance == row.substance && it.source == row.source && it.sequenceNumber == row.sequenceNumber }

            if (existing == null) {
                updatedRecords += row
            } else {
                if (existing.name != row.name) {
                    logger.info { "Synonym name changed for substance ${existing.substance.code}: '${existing.name}' -> '${row.name}'" }
                    updatedRecords += existing.copy(name = row.name)
                }
            }
        }

        substanceSynonymRepository.saveAll(updatedRecords)
        logger.info { "Processed ${updatedRecords.size} updates for MpdSubstanceSynonym." }
    }

    private fun parseLine(line: String, validFromOfNewDataset: LocalDate): MpdSubstanceSynonym? {
        val cols = line.split(";")
        if (cols.size < 4) return null

        val substanceCode = cols[0].trim()
        val sequence = cols[1].toIntOrNull() ?: return null
        val sourceCode = cols[2].trim()
        val name = cols[3].trim()

        val substance = substanceRepository.findByCode(substanceCode) ?: return null
        val source = sourceRepository.findByCode(sourceCode) ?: return null

        return MpdSubstanceSynonym(
            substance = substance,
            sequenceNumber = sequence,
            source = source,
            name = name,
            validFrom = validFromOfNewDataset,
            validTo = null
        )
    }
}
