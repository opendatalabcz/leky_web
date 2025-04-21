package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DatasetSourceConfig {

    @Bean
    fun datasetSources(): List<DatasetSource> = listOf(
        DatasetSource(
            "https://opendata.sukl.cz/?q=katalog/dis-13",
            listOf(
                SourcePattern(
                    Regex("^DIS13_(\\d{4})(\\d{2})v\\d{2}\\.csv$", RegexOption.IGNORE_CASE),
                    FileType.CSV,
                    DatasetType.DISTRIBUTIONS_FROM_DISTRIBUTORS
                )
            )
        ),
        DatasetSource(
            "https://opendata.sukl.cz/?q=katalog/dis-13-zahranici",
            listOf(
                SourcePattern(
                    Regex("^DIS13_ZAHRANICI_(\\d{4})(\\d{2})v\\d{2}\\.csv$", RegexOption.IGNORE_CASE),
                    FileType.CSV,
                    DatasetType.DISTRIBUTIONS_EXPORT_FROM_DISTRIBUTORS
                )
            )
        ),
        DatasetSource(
            "https://opendata.sukl.cz/?q=katalog/reg-13",
            listOf(
                SourcePattern(
                    Regex("^REG13_(\\d{4})(\\d{2})v\\d{2}\\.csv$", RegexOption.IGNORE_CASE),
                    FileType.CSV,
                    DatasetType.DISTRIBUTIONS_FROM_MAHS
                )
            )
        ),
        DatasetSource(
            "https://opendata.sukl.cz/?q=katalog/lek-13",
            listOf(
                SourcePattern(
                    Regex("^LEK13_(\\d{4})(\\d{2})v\\d{2}\\.csv$", RegexOption.IGNORE_CASE),
                    FileType.CSV,
                    DatasetType.DISTRIBUTIONS_FROM_PHARMACIES
                ),
                SourcePattern(
                    Regex("^LEK13_(\\d{4})\\.zip$", RegexOption.IGNORE_CASE),
                    FileType.ZIP,
                    DatasetType.DISTRIBUTIONS_FROM_PHARMACIES
                )
            )
        ),
        DatasetSource(
            "https://opendata.sukl.cz/?q=katalog/predepsane-vydane-lecive-pripravky-ze-systemu-erecept",
            listOf(
                SourcePattern(
                    Regex("^erecept_predpis_(\\d{4})(\\d{2})\\.zip$", RegexOption.IGNORE_CASE),
                    FileType.ZIP,
                    DatasetType.ERECEPT_PRESCRIPTIONS
                ),
                SourcePattern(
                    Regex("^erecept_vydej_(\\d{4})(\\d{2})\\.zip$", RegexOption.IGNORE_CASE),
                    FileType.ZIP,
                    DatasetType.ERECEPT_DISPENSES
                )
            )
        ),
        DatasetSource(
            "https://opendata.sukl.cz/?q=katalog/historie-predepsanych-vydanych-lecivych-pripravku-ze-systemu-erecept",
            listOf(
                SourcePattern(
                    Regex("^erecept_predpis_(\\d{4})\\.zip$", RegexOption.IGNORE_CASE),
                    FileType.ZIP,
                    DatasetType.ERECEPT_PRESCRIPTIONS
                ),
                SourcePattern(
                    Regex("^erecept_vydej_(\\d{4})\\.zip$", RegexOption.IGNORE_CASE),
                    FileType.ZIP,
                    DatasetType.ERECEPT_DISPENSES
                )
            )
        ),
        DatasetSource(
            "https://opendata.sukl.cz/?q=katalog/databaze-lecivych-pripravku-dlp",
            listOf(
                SourcePattern(
                    Regex("^DLP(\\d{4})(\\d{2})\\d{2}\\.zip$", RegexOption.IGNORE_CASE),
                    FileType.ZIP,
                    DatasetType.MEDICINAL_PRODUCT_DATABASE
                )
            )
        ),
        DatasetSource(
            "https://opendata.sukl.cz/?q=katalog/historie-databaze-lecivych-pripravku-dlp",
            listOf(
                SourcePattern(
                    Regex("^DLP(\\d{4})\\.zip$", RegexOption.IGNORE_CASE),
                    FileType.ZIP,
                    DatasetType.MEDICINAL_PRODUCT_DATABASE
                )
            )
        )
    )
}

data class DatasetSource(
    val pageUrl: String,
    val patterns: List<SourcePattern>
)

data class SourcePattern(
    val regex: Regex,
    val fileType: FileType,
    val datasetType: DatasetType
)
