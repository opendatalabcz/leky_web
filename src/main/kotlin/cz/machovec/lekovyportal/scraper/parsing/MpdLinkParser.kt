package cz.machovec.lekovyportal.scraper.parsing

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream

class MpdLinkParser : LinkParser {

    private val fileType = FileType.ZIP
    private val nameRegex =
        Regex("^DLP(\\d{4})(\\d{2})\\d{2}${Regex.escape(fileType.extension)}$", RegexOption.IGNORE_CASE)

    override fun parse(fileUrl: String): ParsedFileInfo? {
        val fileName = fileUrl.substringAfterLast("/")
        val nameMatch = nameRegex.matchEntire(fileName) ?: return null
        val fallbackYear = nameMatch.groupValues[1].toInt()
        val fallbackMonth = nameMatch.groupValues[2].toInt()

        val period = runCatching { determineDatasetPeriod(URL(fileUrl).openStream().readBytes()) }.getOrNull()
        val (year, month) = period ?: Pair(fallbackYear, fallbackMonth)

        return ParsedFileInfo(DatasetType.MPD, fileType, year, month)
    }

    private fun determineDatasetPeriod(zipBytes: ByteArray): Pair<Int, Int>? {
        ZipInputStream(zipBytes.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name == "dlp_platnost.csv") {
                    BufferedReader(InputStreamReader(zis)).use { reader ->
                        reader.readLine()
                        val dataLine = reader.readLine() ?: return null
                        val date = LocalDate.parse(
                            dataLine.split(";")[0],
                            DateTimeFormatter.ofPattern("dd.MM.yyyy")
                        )
                        return Pair(date.year, date.monthValue)
                    }
                }
                entry = zis.nextEntry
            }
        }
        return null
    }
}
