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
            pageUrl = "https://opendata.sukl.cz/?q=katalog/historie-databaze-lecivych-pripravku-dlp",
            patterns = listOf(
                SourcePattern(
                    regex = Regex("^DLP(\\d{4})\\.zip$", RegexOption.IGNORE_CASE),
                    fileType = FileType.ZIP,
                    datasetType = DatasetType.MEDICINAL_PRODUCT_DATABASE,
                    yearGroupIndex = 1
                ),
                SourcePattern(
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
                SourcePattern(
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
                SourcePattern(
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
                SourcePattern(
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
                SourcePattern(
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
                SourcePattern(
                    regex = Regex("^LEK13_(\\d{4})(\\d{2})v\\d{2}\\.csv$", RegexOption.IGNORE_CASE),
                    fileType = FileType.CSV,
                    datasetType = DatasetType.DISTRIBUTIONS_FROM_PHARMACIES,
                    yearGroupIndex = 1,
                    monthGroupIndex = 2
                ),
                SourcePattern(
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
                SourcePattern(
                    regex = Regex("^erecept_predpis_(\\d{4})(\\d{2})\\.zip$", RegexOption.IGNORE_CASE),
                    fileType = FileType.ZIP,
                    datasetType = DatasetType.ERECEPT_PRESCRIPTIONS,
                    yearGroupIndex = 1,
                    monthGroupIndex = 2
                ),
                SourcePattern(
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
                SourcePattern(
                    regex = Regex("^erecept_predpis_(\\d{4})\\.zip$", RegexOption.IGNORE_CASE),
                    fileType = FileType.ZIP,
                    datasetType = DatasetType.ERECEPT_PRESCRIPTIONS,
                    yearGroupIndex = 1
                ),
                SourcePattern(
                    regex = Regex("^erecept_vydej_(\\d{4})\\.zip$", RegexOption.IGNORE_CASE),
                    fileType = FileType.ZIP,
                    datasetType = DatasetType.ERECEPT_DISPENSES,
                    yearGroupIndex = 1
                )
            )
        ),
    )
}


data class DatasetSource(
    val pageUrl: String,
    val patterns: List<SourcePattern>
)

data class SourcePattern(
    val regex: Regex,
    val fileType: FileType,
    val datasetType: DatasetType,
    val yearGroupIndex: Int,
    val monthGroupIndex: Int? = null
) {
    fun extractYearAndMonth(fileName: String): Pair<Int, Int?>? {
        val match = regex.matchEntire(fileName) ?: return null
        val year = match.groupValues.getOrNull(yearGroupIndex)?.toIntOrNull() ?: return null
        val month = monthGroupIndex?.let { idx ->
            match.groupValues.getOrNull(idx)?.toIntOrNull()
        }
        return year to month
    }
}
