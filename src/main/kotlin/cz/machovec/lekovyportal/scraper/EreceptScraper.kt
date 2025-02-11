package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.NewFileMessage
import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class EreceptScraper(
    private val processedDatasetRepository: ProcessedDatasetRepository
) {
    private val ERECEPT_URL = "https://opendata.sukl.cz/?q=katalog/predepsane-vydane-lecive-pripravky-ze-systemu-erecept"
    private val PREFIX_OF_SEARCHED_FILES = "https://opendata.sukl.cz/soubory/ERECEPT/"

    fun scrape(): List<NewFileMessage> {
        val newMessages = mutableListOf<NewFileMessage>()

        // 1) Download HTML
        val doc = Jsoup.connect(ERECEPT_URL).get()

        // 2) Find relevant links
        val links = doc.select("a[href]")
            .map { it.attr("abs:href") }
            .filter { href ->
                href.startsWith(PREFIX_OF_SEARCHED_FILES) && href.endsWith(".zip")
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

        val predpisRegex = Regex("^erecept_predpis_(\\d{4})(\\d{2})\\.zip$", RegexOption.IGNORE_CASE)
        val vydejRegex   = Regex("^erecept_vydej_(\\d{4})(\\d{2})\\.zip$", RegexOption.IGNORE_CASE)

        predpisRegex.matchEntire(fileName)?.let { match ->
            val year = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            return Triple(DatasetType.ERECEPT_PREDPIS, year, month)
        }
        vydejRegex.matchEntire(fileName)?.let { match ->
            val year = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            return Triple(DatasetType.ERECEPT_VYDEJ, year, month)
        }

        return null
    }
}
