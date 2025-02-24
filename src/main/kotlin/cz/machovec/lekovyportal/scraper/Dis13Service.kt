package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.scraper.parsing.Dis13LinkParser
import cz.machovec.lekovyportal.scraper.scraping.HtmlScraper
import org.springframework.stereotype.Service

@Service
class Dis13Service(
    htmlScraper: HtmlScraper,
    dis13LinkParser: Dis13LinkParser,
    processedDatasetRepository: ProcessedDatasetRepository
) : AbstractDatasetService(
    htmlScraper = htmlScraper,
    linkParser = dis13LinkParser,
    processedDatasetRepository = processedDatasetRepository,
    pageUrl = "https://opendata.sukl.cz/?q=katalog/dis-13",
    filePrefix = "https://opendata.sukl.cz/soubory/DIS13/",
    fileSuffix = ".csv"
)