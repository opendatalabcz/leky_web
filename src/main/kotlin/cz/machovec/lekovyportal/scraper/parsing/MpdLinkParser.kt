package cz.machovec.lekovyportal.scraper.parsing

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream

private val logger = KotlinLogging.logger {}

@Component
class MpdLinkParser : LinkParser {

    companion object {
        private val FILE_TYPE = FileType.ZIP
        private val DLP_REGEX = Regex("^DLP(\\d{4})(\\d{2})\\d{2}${Regex.escape(FILE_TYPE.extension)}$", RegexOption.IGNORE_CASE)
    }

    override fun parse(fileUrl: String): ParsedFileInfo? {
        val fileName = fileUrl.substringAfterLast("/")
        val match = DLP_REGEX.matchEntire(fileName) ?: return null

        val zipBytes = try {
            URL(fileUrl).readBytes()
        } catch (e: Exception) {
            logger.error { "Failed to download file: $fileUrl, error: ${e.message}" }
            return null
        }

        val datasetPeriod = determineDatasetPeriod(zipBytes)
        if (datasetPeriod == null) {
            logger.warn { "Could not determine dataset period for $fileUrl, falling back to filename." }
        }

        val (year, month) = datasetPeriod ?: Pair(
            match.groupValues[1].toIntOrNull() ?: return null,
            match.groupValues[2].toIntOrNull() ?: return null
        )

        logger.info { "Determined dataset period: $year-$month for file $fileUrl" }

        return ParsedFileInfo(
            datasetType = DatasetType.MPD,
            fileType = FILE_TYPE,
            year = year,
            month = month
        )
    }

    fun determineDatasetPeriod(zipBytes: ByteArray): Pair<Int, Int>? {
        ZipInputStream(zipBytes.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name == "dlp_platnost.csv") {
                    BufferedReader(InputStreamReader(zis)).use { reader ->
                        val header = reader.readLine()
                        val dataLine = reader.readLine() ?: return null

                        val cols = dataLine.split(";").map { it.trim() }
                        if (cols.size < 2) return null

                        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                        val validFrom = LocalDate.parse(cols[0], formatter)

                        return Pair(validFrom.year, validFrom.monthValue)
                    }
                }
                entry = zis.nextEntry
            }
        }
        return null
    }
}
