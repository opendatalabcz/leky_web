package cz.machovec.lekovyportal.processor.mdp

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import cz.machovec.lekovyportal.domain.entity.mpd.MpdCancelledRegistration
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAdministrationRouteRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCancelledRegistrationRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDosageFormRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdOrganisationRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRegistrationProcessRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRegistrationStatusRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class MpdCancelledRegistrationProcessor(
    private val cancelledRegistrationRepository: MpdCancelledRegistrationRepository,
    private val administrationRouteRepository: MpdAdministrationRouteRepository,
    private val dosageFormRepository: MpdDosageFormRepository,
    private val organisationRepository: MpdOrganisationRepository,
    private val registrationProcessRepository: MpdRegistrationProcessRepository,
    private val registrationStatusRepository: MpdRegistrationStatusRepository,
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    @Transactional
    fun importData(csvBytes: ByteArray, validFromOfNewDataset: LocalDate, validToOfNewDataset: LocalDate?) {
        val inputStream = ByteArrayInputStream(csvBytes)
        val reader = InputStreamReader(inputStream, Charset.forName("Windows-1250"))

        val csvParser = CSVParserBuilder()
            .withSeparator(';')
            .withIgnoreQuotations(false)
            .build()

        val csvReader = CSVReaderBuilder(reader)
            .withCSVParser(csvParser)
            .withSkipLines(1)
            .build()

        val lines = csvReader.readAll()
        val entities = lines.mapNotNullIndexed { index, row -> parseRow(index, row, validFromOfNewDataset) }

        cancelledRegistrationRepository.saveAll(entities)
        println("✅ Zpracováno a uloženo ${entities.size} záznamů zrušené registrace.")
    }

    private fun parseRow(index: Int, row: Array<String>, validFrom: LocalDate): MpdCancelledRegistration? {
        if (row.size < 13) {
            println("Skipping line ${index + 2}: Not enough columns (${row.size})")
            return null
        }

        try {
            val name = row[0].trim()
            val administrationRoute = row[1].trim().takeIf { it.isNotEmpty() }?.let { administrationRouteRepository.findByCode(it) }
            val dosageForm = row[2].trim().takeIf { it.isNotEmpty() }?.let { dosageFormRepository.findByCode(it) }
            val strength = row[3].trim().ifBlank { null }

            val registrationNumber = row[4].trim().ifBlank { null }
            val parallelImportId = row[5].trim().ifBlank { null }
            val mrpNumber = row[6].trim().ifBlank { null }

            val registrationProcess = row[7].trim().takeIf { it.isNotEmpty() }?.let { registrationProcessRepository.findByCode(it) }
            val registrationLegalBasis = row[8].trim().ifBlank { null }

            val mahCode = row[9].trim()
            val mahCountryCode = row[10].trim()
            val marketingAuthorizationHolder = if (mahCode.isNotEmpty() && mahCountryCode.isNotEmpty()) {
                organisationRepository.findByCodeAndCountryCode(mahCode, mahCountryCode)
            } else null

            val registrationEndDate = parseDate(row[11].trim())
            val registrationStatus = row[12].trim().takeIf { it.isNotEmpty() }?.let { registrationStatusRepository.findByCode(it) }

            return MpdCancelledRegistration(
                name = name,
                administrationRoute = administrationRoute,
                dosageForm = dosageForm,
                strength = strength,
                registrationNumber = registrationNumber,
                parallelImportId = parallelImportId,
                mrpNumber = mrpNumber,
                registrationProcess = registrationProcess,
                registrationLegalBasis = registrationLegalBasis,
                marketingAuthorizationHolder = marketingAuthorizationHolder,
                registrationEndDate = registrationEndDate,
                registrationStatus = registrationStatus,
                validFrom = validFrom,
                validTo = null
            )
        } catch (ex: Exception) {
            println("Chyba na řádku ${index + 2}: ${ex.message}")
            return null
        }
    }

    private fun parseDate(raw: String): LocalDate? {
        return raw.takeIf { it.isNotBlank() }?.let {
            try {
                LocalDate.parse(it, dateFormatter)
            } catch (ex: Exception) {
                println("Chybný formát data: $raw")
                null
            }
        }
    }

    private inline fun <T, R : Any> List<T>.mapNotNullIndexed(transform: (index: Int, T) -> R?): List<R> {
        val result = mutableListOf<R>()
        forEachIndexed { index, item -> transform(index, item)?.let { result.add(it) } }
        return result
    }
}