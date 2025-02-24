package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.scraper.parsing.Dis13ZahraniciLinkParser
import cz.machovec.lekovyportal.scraper.scraping.HtmlScraper
import org.springframework.stereotype.Service

@Service
class Dis13ZahraniciService(
    htmlScraper: HtmlScraper,
    dis13ZahraniciLinkParser: Dis13ZahraniciLinkParser,
    processedDatasetRepository: ProcessedDatasetRepository
) : AbstractDatasetService(
    htmlScraper = htmlScraper,
    linkParser = dis13ZahraniciLinkParser,
    processedDatasetRepository = processedDatasetRepository,
    pageUrl = "https://opendata.sukl.cz/?q=katalog/dis-13-zahranici",
    filePrefix = "https://opendata.sukl.cz/soubory/DIS13_ZAHRANICI/",
    fileSuffix = ".csv"
)