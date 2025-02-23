package cz.machovec.lekovyportal.scraperDeprecated

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.NewFileMessage
import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class EreceptScraperDeprecated(
    private val processedDatasetRepository: ProcessedDatasetRepository
) {
    private val FILE_EXTENSION = FileType.ZIP
    private val ERECEPT_URL = "https://opendata.sukl.cz/?q=katalog/predepsane-vydane-lecive-pripravky-ze-systemu-erecept"
    private val PREFIX_OF_SEARCHED_FILES = "https://opendata.sukl.cz/soubory/ERECEPT/"

    private val PREDPIS_REGEX = Regex("^erecept_predpis_(\\d{4})(\\d{2})${Regex.escape(FILE_EXTENSION.extension)}$", RegexOption.IGNORE_CASE)
    private val VYDEJ_REGEX = Regex("^erecept_vydej_(\\d{4})(\\d{2})${Regex.escape(FILE_EXTENSION.extension)}$", RegexOption.IGNORE_CASE)

    fun scrape(): List<NewFileMessage> {
        val newMessages = mutableListOf<NewFileMessage>()

        // 1) Download HTML
        val doc = Jsoup.connect(ERECEPT_URL).get()

        // 2) Find relevant links
        val links = doc.select("a[href]")
            .map { it.attr("abs:href") }
            .filter { href ->
                href.startsWith(PREFIX_OF_SEARCHED_FILES) && href.endsWith(FILE_EXTENSION.extension)
            }

        // 3) For each link -> parse (year, month), checkDB, prepare NewFileMessage
        for (fileUrl in links) {
            val parsed = parseEreceptFileName(fileUrl)
            if (parsed != null) {
                val (datasetType, year, month) = parsed
                val existing = processedDatasetRepository.findByDatasetTypeAndYearAndMonth(
                    datasetType, year, month
                )
                if (existing == null) {
                    val msg = NewFileMessage(
                        datasetType = datasetType,
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

    private fun parseEreceptFileName(fileUrl: String): Triple<DatasetType, Int, Int>? {
        val fileName = fileUrl.substringAfterLast("/")

        PREDPIS_REGEX.matchEntire(fileName)?.let { match ->
            val year = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            return Triple(DatasetType.ERECEPT_PREDPIS, year, month)
        }
        VYDEJ_REGEX.matchEntire(fileName)?.let { match ->
            val year = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            return Triple(DatasetType.ERECEPT_VYDEJ, year, month)
        }

        return null
    }
}
