package cz.machovec.lekovyportal.processor.mdp

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import cz.machovec.lekovyportal.domain.entity.mpd.MpdRegistrationException
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMedicinalProductRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRegistrationExceptionRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

@Service
class MpdRegistrationExceptionProcessor(
    private val registrationExceptionRepository: MpdRegistrationExceptionRepository,
    private val medicinalProductRepository: MpdMedicinalProductRepository
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val expectedColumnCount = 10

    @Transactional
    fun importData(csvBytes: ByteArray, validFromOfNewDataset: LocalDate, validToOfNewDataset: LocalDate?) {
        val inputStream = ByteArrayInputStream(csvBytes)
        val reader = InputStreamReader(inputStream, Charset.forName("Windows-1250"))

        val csvParser = CSVParserBuilder()
            .withSeparator(';')
            .withIgnoreQuotations(false)
            .build()

        val csvReader = CSVReaderBuilder(reader)
            .withSkipLines(1)
            .withCSVParser(csvParser)
            .build()

        val allRows = csvReader.readAll()
        logger.info("Načteno ${allRows.size} řádků ze souboru.")

        var skippedDueToColumnCount = 0
        var skippedDueToInvalidProduct = 0
        var skippedDueToDateParse = 0
        var successfullyParsed = 0

        val currentData = mutableListOf<MpdRegistrationException>()
        allRows.forEachIndexed { index, row ->
            if (row.size != expectedColumnCount) {
                logger.warn("Řádek ${index + 2}: Přeskakuji kvůli špatnému počtu sloupců (${row.size}/$expectedColumnCount): ${row.joinToString(";")}")
                skippedDueToColumnCount++
                return@forEachIndexed
            }

            val parsed = parseRow(row, validFromOfNewDataset)
            if (parsed == null) {
                if (medicinalProductRepository.findBySuklCode(row[0].trim()) == null) {
                    logger.warn("Řádek ${index + 2}: SUKL kód ${row[0].trim()} nenalezen v databázi – řádek přeskakuji.")
                    skippedDueToInvalidProduct++
                } else {
                    logger.warn("Řádek ${index + 2}: Chyba při parsování – řádek přeskakuji: ${row.joinToString(";")}")
                    skippedDueToDateParse++
                }
            } else {
                currentData.add(parsed)
                successfullyParsed++
            }
        }

        logger.info("Zpracováno ${successfullyParsed} validních záznamů, přeskočeno ${skippedDueToColumnCount} kvůli počtu sloupců, ${skippedDueToInvalidProduct} kvůli nenalezenému SUKL kódu, ${skippedDueToDateParse} kvůli jiné chybě.")

        val existingRecords = registrationExceptionRepository.findAll()
        val updatedRecords = mutableListOf<MpdRegistrationException>()
        var unchangedRecords = 0
        var newRecords = 0
        var modifiedRecords = 0

        currentData.forEach { newRecord ->
            val existing = existingRecords.find {
                it.medicinalProduct.suklCode == newRecord.medicinalProduct.suklCode &&
                        it.validFrom == newRecord.validFrom
            }

            if (existing == null) {
                updatedRecords += newRecord
                newRecords++
            } else {
                var changed = false
                if (existing.allowedPackageCount != newRecord.allowedPackageCount ||
                    existing.purpose != newRecord.purpose ||
                    existing.workplace != newRecord.workplace ||
                    existing.distributor != newRecord.distributor ||
                    existing.note != newRecord.note ||
                    existing.submitter != newRecord.submitter ||
                    existing.manufacturer != newRecord.manufacturer) {
                    changed = true
                }

                if (changed) {
                    updatedRecords += existing.copy(
                        allowedPackageCount = newRecord.allowedPackageCount,
                        purpose = newRecord.purpose,
                        workplace = newRecord.workplace,
                        distributor = newRecord.distributor,
                        note = newRecord.note,
                        submitter = newRecord.submitter,
                        manufacturer = newRecord.manufacturer,
                        validTo = null
                    )
                    modifiedRecords++
                } else {
                    unchangedRecords++
                }
            }
        }

        logger.info("Nové záznamy: $newRecords, upravené záznamy: $modifiedRecords, beze změny: $unchangedRecords.")

        registrationExceptionRepository.saveAll(updatedRecords)
        logger.info("Uloženo ${updatedRecords.size} záznamů do DB.")
    }

    private fun parseRow(row: Array<String>, validFromOfNewDataset: LocalDate): MpdRegistrationException? {
        return try {
            val suklCode = row[0].trim()
            val medicinalProduct = medicinalProductRepository.findBySuklCode(suklCode) ?: return null

            val validFrom = parseDate(row[1].trim()) ?: return null
            val validTo = parseDate(row[2].trim())

            val allowedPackageCount = row[3].trim().toIntOrNull()

            MpdRegistrationException(
                medicinalProduct = medicinalProduct,
                validFrom = validFrom,
                validTo = validTo,
                allowedPackageCount = allowedPackageCount,
                purpose = row[4].trim().ifBlank { null },
                workplace = row[5].trim().ifBlank { null },
                distributor = row[6].trim().ifBlank { null },
                note = row[7].trim().ifBlank { null },
                submitter = row[8].trim().ifBlank { null },
                manufacturer = row[9].trim().ifBlank { null }
            )
        } catch (e: Exception) {
            logger.error("Chyba při parsování řádku: ${row.joinToString(";")}", e)
            null
        }
    }

    private fun parseDate(raw: String): LocalDate? {
        if (raw.isBlank()) return null
        return try {
            LocalDate.parse(raw, dateFormatter)
        } catch (e: Exception) {
            logger.warn("Chybný formát data: $raw", e)
            null
        }
    }
}
