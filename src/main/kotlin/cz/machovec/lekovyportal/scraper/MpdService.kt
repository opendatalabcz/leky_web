package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.scraper.parsing.MpdLinkParser
import cz.machovec.lekovyportal.scraper.scraping.HtmlScraper
import org.springframework.stereotype.Service

@Service
class MpdService(
    htmlScraper: HtmlScraper,
    mpdLinkParser: MpdLinkParser,
    processedDatasetRepository: ProcessedDatasetRepository
) : AbstractDatasetService(
    htmlScraper = htmlScraper,
    linkParser = mpdLinkParser,
    processedDatasetRepository = processedDatasetRepository,
    pageUrl = "https://opendata.sukl.cz/?q=katalog/databaze-lecivych-pripravku-dlp",
    filePrefix = "https://opendata.sukl.cz/soubory/SOD",
    fileSuffix = ".zip"
)
