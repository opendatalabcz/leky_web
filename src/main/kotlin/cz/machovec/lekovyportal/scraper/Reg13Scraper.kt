package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.NewFileMessage
import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class Reg13Scraper(
    private val reg13Parser: Reg13Parser,
    private val processedDatasetRepository: ProcessedDatasetRepository
) {
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
                href.startsWith(PREFIX_OF_SEARCHED_FILES) && href.endsWith(".csv")
            }

        // 3) For each link -> parse (year, month), checkDB, prepare NewFileMessage
        for (fileUrl in links) {
            val parsed = reg13Parser.parseReg13FileName(fileUrl)
            if (parsed != null) {
                val (year, month) = parsed
                val existing = processedDatasetRepository.findByDatasetTypeAndYearAndMonth(
                    DatasetType.DISTRIBUCE_REG, year, month
                )
                if (existing == null) {
                    val msg = NewFileMessage(
                        datasetType = DatasetType.DISTRIBUCE_REG,
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
}
