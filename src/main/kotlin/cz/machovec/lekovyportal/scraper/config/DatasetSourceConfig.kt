package cz.machovec.lekovyportal.scraper.config

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.core.domain.dataset.FileType
import cz.machovec.lekovyportal.scraper.model.DatasetSource
import cz.machovec.lekovyportal.scraper.model.DatasetSourcePattern
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DatasetSourceConfig {

    @Bean
    fun datasetSources(): List<DatasetSource> = listOf(
        DatasetSource(
            pageUrl = "https://opendata.sukl.cz/?q=katalog/historie-databaze-lecivych-pripravku-dlp",
            patterns = listOf(
                DatasetSourcePattern(
                    regex = Regex("^DLP(\\d{4})\\.zip$", RegexOption.IGNORE_CASE),
                    fileType = FileType.ZIP,
                    datasetType = DatasetType.MEDICINAL_PRODUCT_DATABASE,
                    yearGroupIndex = 1
                ),
                DatasetSourcePattern(
                    regex = Regex("^DLP(\\d{4})(\\d{2})\\.zip$", RegexOption.IGNORE_CASE),
                    fileType = FileType.ZIP,
                    datasetType = DatasetType.MEDICINAL_PRODUCT_DATABASE,
                    yearGroupIndex = 1,
                    monthGroupIndex = 2
                )
            )
        ),
        DatasetSource(
            pageUrl = "https://opendata.sukl.cz/?q=katalog/databaze-lecivych-pripravku-dlp",
            patterns = listOf(
                DatasetSourcePattern(
                    regex = Regex("^DLP(\\d{4})(\\d{2})\\d{2}\\.zip$", RegexOption.IGNORE_CASE),
                    fileType = FileType.ZIP,
                    datasetType = DatasetType.MEDICINAL_PRODUCT_DATABASE,
                    yearGroupIndex = 1,
                    monthGroupIndex = 2
                )
            )
        ),
        DatasetSource(
            pageUrl = "https://opendata.sukl.cz/?q=katalog/dis-13",
            patterns = listOf(
                DatasetSourcePattern(
                    regex = Regex("^DIS13_(\\d{4})(\\d{2})v\\d{2}\\.csv$", RegexOption.IGNORE_CASE),
                    fileType = FileType.CSV,
                    datasetType = DatasetType.DISTRIBUTIONS_FROM_DISTRIBUTORS,
                    yearGroupIndex = 1,
                    monthGroupIndex = 2
                )
            )
        ),
        DatasetSource(
            pageUrl = "https://opendata.sukl.cz/?q=katalog/dis-13-zahranici",
            patterns = listOf(
                DatasetSourcePattern(
                    regex = Regex("^DIS13_ZAHRANICI_(\\d{4})(\\d{2})v\\d{2}\\.csv$", RegexOption.IGNORE_CASE),
                    fileType = FileType.CSV,
                    datasetType = DatasetType.DISTRIBUTIONS_EXPORT_FROM_DISTRIBUTORS,
                    yearGroupIndex = 1,
                    monthGroupIndex = 2
                )
            )
        ),
        DatasetSource(
            pageUrl = "https://opendata.sukl.cz/?q=katalog/reg-13",
            patterns = listOf(
                DatasetSourcePattern(
                    regex = Regex("^REG13_(\\d{4})(\\d{2})v\\d{2}\\.csv$", RegexOption.IGNORE_CASE),
                    fileType = FileType.CSV,
                    datasetType = DatasetType.DISTRIBUTIONS_FROM_MAHS,
                    yearGroupIndex = 1,
                    monthGroupIndex = 2
                )
            )
        ),
        DatasetSource(
            pageUrl = "https://opendata.sukl.cz/?q=katalog/lek-13",
            patterns = listOf(
                DatasetSourcePattern(
                    regex = Regex("^LEK13_(\\d{4})(\\d{2})v\\d{2}\\.csv$", RegexOption.IGNORE_CASE),
                    fileType = FileType.CSV,
                    datasetType = DatasetType.DISTRIBUTIONS_FROM_PHARMACIES,
                    yearGroupIndex = 1,
                    monthGroupIndex = 2
                ),
                DatasetSourcePattern(
                    regex = Regex("^LEK13_(\\d{4})\\.zip$", RegexOption.IGNORE_CASE),
                    fileType = FileType.ZIP,
                    datasetType = DatasetType.DISTRIBUTIONS_FROM_PHARMACIES,
                    yearGroupIndex = 1
                )
            )
        ),
        DatasetSource(
            pageUrl = "https://opendata.sukl.cz/?q=katalog/predepsane-vydane-lecive-pripravky-ze-systemu-erecept",
            patterns = listOf(
                DatasetSourcePattern(
                    regex = Regex("^erecept_predpis_(\\d{4})(\\d{2})\\.zip$", RegexOption.IGNORE_CASE),
                    fileType = FileType.ZIP,
                    datasetType = DatasetType.ERECEPT_PRESCRIPTIONS,
                    yearGroupIndex = 1,
                    monthGroupIndex = 2
                ),
                DatasetSourcePattern(
                    regex = Regex("^erecept_vydej_(\\d{4})(\\d{2})\\.zip$", RegexOption.IGNORE_CASE),
                    fileType = FileType.ZIP,
                    datasetType = DatasetType.ERECEPT_DISPENSES,
                    yearGroupIndex = 1,
                    monthGroupIndex = 2
                )
            )
        ),
        DatasetSource(
            pageUrl = "https://opendata.sukl.cz/?q=katalog/historie-predepsanych-vydanych-lecivych-pripravku-ze-systemu-erecept",
            patterns = listOf(
                DatasetSourcePattern(
                    regex = Regex("^erecept_predpis_(\\d{4})\\.zip$", RegexOption.IGNORE_CASE),
                    fileType = FileType.ZIP,
                    datasetType = DatasetType.ERECEPT_PRESCRIPTIONS,
                    yearGroupIndex = 1
                ),
                DatasetSourcePattern(
                    regex = Regex("^erecept_vydej_(\\d{4})\\.zip$", RegexOption.IGNORE_CASE),
                    fileType = FileType.ZIP,
                    datasetType = DatasetType.ERECEPT_DISPENSES,
                    yearGroupIndex = 1
                )
            )
        ),
    )
}
