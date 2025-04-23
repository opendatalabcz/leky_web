package cz.machovec.lekovyportal

import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream

@Component
class MpdDatasetValidityResolver {
    // TODO: review
    companion object {
        private const val VALIDITY_FILE_NAME = "dlp_platnost.csv"
    }

    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    fun resolveFromZip(zipBytes: ByteArray): DatasetValidity? {
        ZipInputStream(zipBytes.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name == VALIDITY_FILE_NAME) {
                    BufferedReader(InputStreamReader(zis)).use { reader ->
                        reader.readLine() // skip header
                        val line = reader.readLine() ?: return null
                        return parseValidityLine(line)
                    }
                }
                entry = zis.nextEntry
            }
        }
        return null
    }

    private fun parseValidityLine(line: String): DatasetValidity? {
        val parts = line.split(";")
        if (parts.isEmpty()) return null

        val validFrom = runCatching { LocalDate.parse(parts[0], formatter) }.getOrNull() ?: return null
        val validTo = parts.getOrNull(1)?.takeIf { it.isNotBlank() }?.let {
            runCatching { LocalDate.parse(it, formatter) }.getOrNull()
        }

        return DatasetValidity(validFrom, validTo)
    }
}

data class DatasetValidity(
    val validFrom: LocalDate,
    val validTo: LocalDate?
)
