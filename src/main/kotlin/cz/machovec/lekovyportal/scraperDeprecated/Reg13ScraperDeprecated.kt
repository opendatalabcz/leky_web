package cz.machovec.lekovyportal.scraperDeprecated

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.NewFileMessage
import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class Reg13ScraperDeprecated(
    private val processedDatasetRepository: ProcessedDatasetRepository
) {
    private val FILE_EXTENSION = FileType.CSV
    private val PARSE_REGEX = Regex("^REG13_(\\d{4})(\\d{2})v\\d{2}${Regex.escape(FILE_EXTENSION.extension)}$", RegexOption.IGNORE_CASE)
    private val REG13_URL = "https://opendata.sukl.cz/?q=katalog/reg-13"
    private val PREFIX_OF_SEARCHED_FILES = "https://opendata.sukl.cz/soubory/REG13/"

    fun scrape(): List<NewFileMessage> {
        val newMessages = mutableListOf<NewFileMessage>()

        // 1) Download HTML
        val doc = Jsoup.connect(REG13_URL).get()

        // 2) Find relevant links
        val links = doc.select("a[href]")
            .map { it.attr("abs:href") }
            .filter { href ->
                href.startsWith(PREFIX_OF_SEARCHED_FILES) && href.endsWith(FILE_EXTENSION.extension)
            }

        // 3) For each link -> parse (year, month), checkDB, prepare NewFileMessage
        for (fileUrl in links) {
            val parsed = parseFileName(fileUrl)
            if (parsed != null) {
                val (year, month) = parsed
                val existing = processedDatasetRepository.findByDatasetTypeAndYearAndMonth(
                    DatasetType.DISTRIBUCE_REG, year, month
                )
                if (existing == null) {
                    val msg = NewFileMessage(
                        datasetType = DatasetType.DISTRIBUCE_REG,
                        fileType = FILE_EXTENSION,
                        year = year,
                        month = month,
                        fileUrl = fileUrl
                    )
                    newMessages += msg
                }
            }
        }

        return newMessages
    }

    private fun parseFileName(fileUrl: String): Pair<Int, Int>? {
        val fileName = fileUrl.substringAfterLast("/")
        val match = PARSE_REGEX.matchEntire(fileName) ?: return null

        val year = match.groupValues[1].toIntOrNull() ?: return null
        val month = match.groupValues[2].toIntOrNull() ?: return null

        return Pair(year, month)
    }
}
