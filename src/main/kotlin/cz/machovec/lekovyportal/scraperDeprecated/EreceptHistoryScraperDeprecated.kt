package cz.machovec.lekovyportal.scraperDeprecated

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.NewFileMessage
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class EreceptHistoryScraperDeprecated(
    private val processedDatasetRepository: ProcessedDatasetRepository
) {
    private val FILE_EXTENSION = FileType.ZIP
    private val ERECEPT_HISTORY_URL =
        "https://opendata.sukl.cz/?q=katalog/historie-predepsanych-vydanych-lecivych-pripravku-ze-systemu-erecept"
    private val PREFIX = "https://opendata.sukl.cz/soubory/ERECEPT_HISTORIE/"

    private val PREDPIS_REGEX = Regex("^erecept_predpis_(\\d{4})${Regex.escape(FILE_EXTENSION.extension)}$", RegexOption.IGNORE_CASE)
    private val VYDEJ_REGEX = Regex("^erecept_vydej_(\\d{4})${Regex.escape(FILE_EXTENSION.extension)}$", RegexOption.IGNORE_CASE)

    fun scrape(): List<NewFileMessage> {
        val newMessages = mutableListOf<NewFileMessage>()

        val doc = Jsoup.connect(ERECEPT_HISTORY_URL).get()
        val links = doc.select("a[href]")
            .map { it.attr("abs:href") }
            .filter { it.startsWith(PREFIX) && it.endsWith(FILE_EXTENSION.extension) }

        for (fileUrl in links) {
            val parsed = parseFileName(fileUrl)
            if (parsed != null) {
                val (datasetType, year) = parsed
                if (isYearIncomplete(datasetType, year)) {
                    val msg = NewFileMessage(
                        datasetType = datasetType,
                        fileType = FILE_EXTENSION,
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

        PREDPIS_REGEX.matchEntire(fileName)?.let {
            val year = it.groupValues[1].toInt()
            return Pair(DatasetType.ERECEPT_PREDPIS, year)
        }
        VYDEJ_REGEX.matchEntire(fileName)?.let {
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