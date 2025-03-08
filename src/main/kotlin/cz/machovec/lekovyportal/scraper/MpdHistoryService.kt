package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.scraper.parsing.MpdHistoryLinkParser
import cz.machovec.lekovyportal.scraper.scraping.HtmlScraper
import org.springframework.stereotype.Service

@Service
class MpdHistoryService(
    htmlScraper: HtmlScraper,
    mpdHistoryLinkParser: MpdHistoryLinkParser,
    processedDatasetRepository: ProcessedDatasetRepository
) : AbstractDatasetService(
    htmlScraper = htmlScraper,
    linkParser = mpdHistoryLinkParser,
    processedDatasetRepository = processedDatasetRepository,
    pageUrl = "https://opendata.sukl.cz/?q=katalog/historie-databaze-lecivych-pripravku-dlp",
    filePrefix = "https://opendata.sukl.cz/soubory/SOD",
    fileSuffix = ".zip"
)
