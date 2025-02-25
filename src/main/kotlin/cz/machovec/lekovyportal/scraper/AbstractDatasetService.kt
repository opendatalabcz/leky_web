package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.NewFileMessage
import cz.machovec.lekovyportal.scraper.parsing.LinkParser
import cz.machovec.lekovyportal.scraper.parsing.ParsedFileInfo
import cz.machovec.lekovyportal.scraper.scraping.HtmlScraper

abstract class AbstractDatasetService(
    private val htmlScraper: HtmlScraper,
    private val linkParser: LinkParser,
    val processedDatasetRepository: ProcessedDatasetRepository,

    private val pageUrl: String,
    private val filePrefix: String,
    private val fileSuffix: String
) {

    open fun collectNewMessages(): List<NewFileMessage> {
        val result = mutableListOf<NewFileMessage>()

        val links = htmlScraper.scrapeLinks(pageUrl, filePrefix, fileSuffix)

        for (link in links) {
            val info = linkParser.parse(link) ?: continue

            if (!isAlreadyInDb(info)) {
                val msg = toNewFileMessage(info, link)
                result += msg
            }
        }
        return result
    }

    protected open fun isAlreadyInDb(info: ParsedFileInfo): Boolean {
        val datasetType = info.datasetType
        val year = info.year
        val month = info.month ?: 0
        return processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(datasetType, year, month)
    }

    protected open fun toNewFileMessage(info: ParsedFileInfo, link: String): NewFileMessage {
        return NewFileMessage(
            datasetType = info.datasetType,
            fileType = info.fileType,
            year = info.year,
            month = info.month,
            fileUrl = link
        )
    }
}
