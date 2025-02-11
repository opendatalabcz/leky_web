package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.NewFileMessage
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class EreceptHistoryScraper(
    private val processedDatasetRepository: ProcessedDatasetRepository
) {
    private val ERECEPT_HISTORY_URL =
        "https://opendata.sukl.cz/?q=katalog/historie-predepsanych-vydanych-lecivych-pripravku-ze-systemu-erecept"
    private val PREFIX = "https://opendata.sukl.cz/soubory/ERECEPT_HISTORIE/"

    fun scrape(): List<NewFileMessage> {
        val newMessages = mutableListOf<NewFileMessage>()

        val doc = Jsoup.connect(ERECEPT_HISTORY_URL).get()
        val links = doc.select("a[href]")
            .map { it.attr("abs:href") }
            .filter { it.startsWith(PREFIX) && it.endsWith(".zip") }

        for (fileUrl in links) {
            val parsed = parseFileName(fileUrl)
            if (parsed != null) {
                val (datasetType, year) = parsed
                if (isYearIncomplete(datasetType, year)) {
                    val msg = NewFileMessage(
                        datasetType = datasetType,
                        year = year,
                        month = null,
                        fileUrl = fileUrl
                    )
                    newMessages += msg
                }
            }
        }
        return newMessages
    }

    private fun parseFileName(fileUrl: String): Pair<DatasetType, Int>? {
        val fileName = fileUrl.substringAfterLast("/")
        val predpisRegex = Regex("^erecept_predpis_(\\d{4})\\.zip$", RegexOption.IGNORE_CASE)
        val vydejRegex = Regex("^erecept_vydej_(\\d{4})\\.zip$", RegexOption.IGNORE_CASE)

        predpisRegex.matchEntire(fileName)?.let {
            val year = it.groupValues[1].toInt()
            return Pair(DatasetType.ERECEPT_PREDPIS, year)
        }
        vydejRegex.matchEntire(fileName)?.let {
            val year = it.groupValues[1].toInt()
            return Pair(DatasetType.ERECEPT_VYDEJ, year)
        }
        return null
    }

    private fun isYearIncomplete(datasetType: DatasetType, year: Int): Boolean {
        val currentDate = LocalDate.now()
        val currentYear = currentDate.year

        val lastMonth = if (year < currentYear) {
            12
        } else if (year == currentYear) {
            currentDate.monthValue - 1
        } else {
            return false
        }

        if (lastMonth <= 0) {
            return false
        }

        for (m in 1..lastMonth) {
            val existing = processedDatasetRepository
                .findByDatasetTypeAndYearAndMonth(datasetType, year, m)
            if (existing == null) {
                return true
            }
        }
        return false
    }
}