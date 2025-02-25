package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.scraper.parsing.EreceptLinkParser
import cz.machovec.lekovyportal.scraper.scraping.HtmlScraper
import org.springframework.stereotype.Service

@Service
class EreceptService(
    htmlScraper: HtmlScraper,
    ereceptLinkParser: EreceptLinkParser,
    processedDatasetRepository: ProcessedDatasetRepository
) : AbstractDatasetService(
    htmlScraper = htmlScraper,
    linkParser = ereceptLinkParser,
    processedDatasetRepository = processedDatasetRepository,
    pageUrl = "https://opendata.sukl.cz/?q=katalog/predepsane-vydane-lecive-pripravky-ze-systemu-erecept",
    filePrefix = "https://opendata.sukl.cz/soubory/ERECEPT/",
    fileSuffix = ".zip"
)
