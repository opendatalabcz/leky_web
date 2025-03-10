package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdPackageType
import cz.machovec.lekovyportal.domain.repository.mpd.MpdPackageTypeRepository
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.nio.charset.Charset
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdPackageTypeProcessor(
    private val packageTypeRepository: MpdPackageTypeRepository
) {

    @Transactional
    fun importData(csvBytes: ByteArray, validFromOfNewDataset: LocalDate, validToOfNewDataset: LocalDate?) {
        val text = csvBytes.toString(Charset.forName("Windows-1250")) // Správná konverze kódování
        val lines = text.split("\r\n", "\n").drop(1).filter { it.isNotBlank() }
        val currentData = lines.mapNotNull { parseLine(it, validFromOfNewDataset) }
        val newCodes = currentData.map { it.code }.toSet()
        val existingRecords = packageTypeRepository.findAllByCodeIn(newCodes)
        val updatedRecords = mutableListOf<MpdPackageType>()

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
                if (existing.nameEn != row.nameEn) {
                    logger.info { "Code ${existing.code} English name changed from '${existing.nameEn}' to '${row.nameEn}'" }
                    changed = true
                }
                if (existing.edqmCode != row.edqmCode) {
                    logger.info { "Code ${existing.code} EDQM code changed from '${existing.edqmCode}' to '${row.edqmCode}'" }
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

        packageTypeRepository.saveAll(updatedRecords)
        logger.info { "Processed ${updatedRecords.size} updates for MpdPackageType." }
    }

    private fun parseLine(line: String, validFromOfNewDataset: LocalDate): MpdPackageType? {
        val cols = line.split(";")
        if (cols.size < 4) return null
        val code = cols[0].trim()
        val name = cols[1].trim()
        val nameEn = cols[2].trim()
        val edqmCode = cols[3].takeIf { it.isNotEmpty() }?.trim()?.toLongOrNull()

        return MpdPackageType(
            code = code,
            name = name,
            nameEn = nameEn,
            edqmCode = edqmCode,
            validFrom = validFromOfNewDataset,
            validTo = null
        )
    }
}
