package cz.machovec.lekovyportal

import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class MpdValidityReader {

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    private val logger = KotlinLogging.logger {}

    fun getValidityFromCsv(csvBytes: ByteArray): MpdValidity? {
        val reader = BufferedReader(InputStreamReader(csvBytes.inputStream()))
        reader.readLine() // Skip header
        val line = reader.readLine()
        if (line == null) {
            logger.warn { "Unable to resolve dataset validity" }
            return null
        }

        return parseValidityLine(line)
    }

    private fun parseValidityLine(line: String): MpdValidity? {
        val parts = line.split(";")
        if (parts.size < 2) return null

        val validFrom = runCatching { LocalDate.parse(parts[0], DATE_FORMATTER) }.getOrNull()
        val validTo = runCatching { LocalDate.parse(parts[1], DATE_FORMATTER) }.getOrNull()

        if (validFrom == null || validTo == null) return null

        return MpdValidity(validFrom, validTo)
    }
}
