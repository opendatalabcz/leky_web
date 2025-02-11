package cz.machovec.lekovyportal.scraper2_0

import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.scraper2_0.parsing.Dis13LinkParser
import cz.machovec.lekovyportal.scraper2_0.scraping.HtmlScraper
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