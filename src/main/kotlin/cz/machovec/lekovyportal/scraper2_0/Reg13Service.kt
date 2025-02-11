package cz.machovec.lekovyportal.scraper2_0

import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.scraper2_0.parsing.Reg13LinkParser
import cz.machovec.lekovyportal.scraper2_0.scraping.HtmlScraper
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
