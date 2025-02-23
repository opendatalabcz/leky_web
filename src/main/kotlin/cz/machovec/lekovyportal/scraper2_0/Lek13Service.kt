package cz.machovec.lekovyportal.scraper2_0

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.NewFileMessage
import cz.machovec.lekovyportal.scraper2_0.parsing.Lek13LinkParser
import cz.machovec.lekovyportal.scraper2_0.scraping.HtmlScraper
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class Lek13Service(
    private val htmlScraper: HtmlScraper,
    private val linkParser: Lek13LinkParser,
    private val processedDatasetRepository: ProcessedDatasetRepository
) {
    private val PAGE_URL = "https://opendata.sukl.cz/?q=katalog/lek-13"
    private val PREFIX = "https://opendata.sukl.cz/soubory/LEK13/"
    private val CSV_SUFFIX = ".csv"
    private val ZIP_SUFFIX = ".zip"

    fun collectNewMessages(): List<NewFileMessage> {
        val result = mutableListOf<NewFileMessage>()

        val csvLinks = htmlScraper.scrapeLinks(PAGE_URL, PREFIX, CSV_SUFFIX)
        val zipLinks = htmlScraper.scrapeLinks(PAGE_URL, PREFIX, ZIP_SUFFIX)
        val links = csvLinks + zipLinks

        links.forEach { link ->
            val info = linkParser.parse(link) ?: return@forEach

            if (info.month != null) {
                if (!isMonthProcessed(info.datasetType, info.year, info.month)) {
                    result += NewFileMessage(
                        datasetType = info.datasetType,
                        fileType = info.fileType,
                        year = info.year,
                        month = info.month,
                        fileUrl = link
                    )
                }
            } else {
                if (isYearIncomplete(info.datasetType, info.year)) {
                    result += NewFileMessage(
                        datasetType = info.datasetType,
                        fileType = info.fileType,
                        year = info.year,
                        month = null,
                        fileUrl = link
                    )
                }
            }
        }

        return result
    }

    private fun isMonthProcessed(datasetType: DatasetType, year: Int, month: Int): Boolean {
        return processedDatasetRepository.findByDatasetTypeAndYearAndMonth(datasetType, year, month) != null
    }

    private fun isYearIncomplete(datasetType: DatasetType, year: Int): Boolean {
        val currentDate = LocalDate.now()
        val currentYear = currentDate.year

        val lastMonth = when {
            year < currentYear -> 12
            year == currentYear -> currentDate.monthValue - 1
            else -> return false
        }

        if (lastMonth <= 0) return false

        for (m in 1..lastMonth) {
            if (!isMonthProcessed(datasetType, year, m)) {
                return true
            }
        }
        return false
    }
}
