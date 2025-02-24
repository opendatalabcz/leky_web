package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.scraper.parsing.EreceptHistoryLinkParser
import cz.machovec.lekovyportal.scraper.parsing.ParsedFileInfo
import cz.machovec.lekovyportal.scraper.scraping.HtmlScraper
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class EreceptHistoryService(
    htmlScraper: HtmlScraper,
    ereceptHistoryParser: EreceptHistoryLinkParser,
    processedDatasetRepository: ProcessedDatasetRepository
) : AbstractDatasetService(
    htmlScraper = htmlScraper,
    linkParser = ereceptHistoryParser,
    processedDatasetRepository = processedDatasetRepository,
    pageUrl = "https://opendata.sukl.cz/?q=katalog/historie-predepsanych-vydanych-lecivych-pripravku-ze-systemu-erecept",
    filePrefix = "https://opendata.sukl.cz/soubory/TODO/",
    fileSuffix = ".zip"
) {

    override fun isAlreadyInDb(info: ParsedFileInfo): Boolean {
        if (info.month != null) {
            return super.isAlreadyInDb(info)
        }
        return !isYearIncomplete(info.datasetType, info.year)
    }

    private fun isYearIncomplete(datasetType: DatasetType, year: Int): Boolean {
        val now = LocalDate.now()
        val currentYear = now.year

        val lastMonth = when {
            year < currentYear -> 12
            year == currentYear -> (now.monthValue - 1).coerceAtLeast(0)
            else -> return false
        }

        if (lastMonth == 0) {
            return false
        }

        for (m in 1..lastMonth) {
            val existing = processedDatasetRepository.findByDatasetTypeAndYearAndMonth(datasetType, year, m)
            if (existing == null) {
                return true
            }
        }
        return false
    }
}
