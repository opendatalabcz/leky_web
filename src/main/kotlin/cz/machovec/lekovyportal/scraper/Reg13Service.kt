package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.scraper.parsing.Reg13LinkParser
import cz.machovec.lekovyportal.scraper.scraping.HtmlScraper
import org.springframework.stereotype.Service

@Service
class Reg13Service(
    htmlScraper: HtmlScraper,
    reg13Parser: Reg13LinkParser,
    processedDatasetRepository: ProcessedDatasetRepository
) : AbstractDatasetService(
    htmlScraper = htmlScraper,
    linkParser = reg13Parser,
    processedDatasetRepository = processedDatasetRepository,
    pageUrl = "https://opendata.sukl.cz/?q=katalog/reg-13",
    filePrefix = "https://opendata.sukl.cz/soubory/REG13/",
    fileSuffix = ".csv"
)
