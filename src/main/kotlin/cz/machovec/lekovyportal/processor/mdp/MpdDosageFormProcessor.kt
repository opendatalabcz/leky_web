package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDosageForm
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDosageFormRepository
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.nio.charset.Charset
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class MpdDosageFormProcessor(
    private val dosageFormRepository: MpdDosageFormRepository
) {

    @Transactional
    fun importData(csvBytes: ByteArray, validFromOfNewDataset: LocalDate, validToOfNewDataset: LocalDate?) {
        val text = csvBytes.toString(Charset.forName("Windows-1250"))
        val lines = text.split("\r\n", "\n").drop(1).filter { it.isNotBlank() }

        val currentData = lines.mapNotNull { parseLine(it, validFromOfNewDataset) }
        val newCodes = currentData.map { it.code }.toSet()
        val existingRecords = dosageFormRepository.findAllByCodeIn(newCodes)
        val updatedRecords = mutableListOf<MpdDosageForm>()

        currentData.forEach { row ->
            val existing = existingRecords.find { it.code == row.code }
            if (existing == null) {
                updatedRecords += row
            } else {
                var changed = false

                if (existing.name != row.name || existing.nameEn != row.nameEn || existing.nameLat != row.nameLat) {
                    logger.info { "Code ${existing.code} name changed: '${existing.name}' -> '${row.name}', '${existing.nameEn}' -> '${row.nameEn}', '${existing.nameLat}' -> '${row.nameLat}'" }
                    changed = true
                }

                if (existing.isCannabis != row.isCannabis) {
                    logger.info { "Code ${existing.code} cannabis flag changed from '${existing.isCannabis}' to '${row.isCannabis}'" }
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
                        nameLat = row.nameLat,
                        isCannabis = row.isCannabis,
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

        dosageFormRepository.saveAll(updatedRecords)
        logger.info { "Processed ${updatedRecords.size} updates for MpdDosageForm." }
    }

    private fun parseLine(line: String, validFromOfNewDataset: LocalDate): MpdDosageForm? {
        val cols = line.split(";")
        if (cols.size < 6) return null

        val code = cols[0].trim()
        val name = cols[1].trim()
        val nameEn = cols[2].trim()
        val nameLat = cols[3].trim()
        val isCannabis = cols[4].trim().equals("A", ignoreCase = true)
        val edqmCode = cols[5].takeIf { it.isNotEmpty() }?.trim()?.toLongOrNull()

        return MpdDosageForm(
            code = code,
            name = name,
            nameEn = nameEn,
            nameLat = nameLat,
            isCannabis = isCannabis,
            edqmCode = edqmCode,
            validFrom = validFromOfNewDataset,
            validTo = null
        )
    }
}
