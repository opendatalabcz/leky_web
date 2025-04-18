package cz.machovec.lekovyportal.scraper.parsing

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ParsersConfig {

    @Bean
    fun dis13LinkParser(): LinkParser =
        RegexLinkParser(
            DatasetType.DISTRIBUTION_DIS,
            FileType.CSV,
            Regex("^DIS13_(\\d{4})(\\d{2})v\\d{2}\\.csv$", RegexOption.IGNORE_CASE)
        )

    @Bean
    fun dis13ZahraniciLinkParser(): LinkParser =
        RegexLinkParser(
            DatasetType.DISTRIBUTION_DIS_ABROAD,
            FileType.CSV,
            Regex("^DIS13_ZAHRANICI_(\\d{4})(\\d{2})v\\d{2}\\.csv$", RegexOption.IGNORE_CASE)
        )

    @Bean
    fun reg13LinkParser(): LinkParser =
        RegexLinkParser(
            DatasetType.DISTRIBUTION_REG,
            FileType.CSV,
            Regex("^REG13_(\\d{4})(\\d{2})v\\d{2}\\.csv$", RegexOption.IGNORE_CASE)
        )

    @Bean
    fun lek13LinkParser(): LinkParser =
        MultiRegexLinkParser(
            listOf(
                MultiRegexLinkParser.Entry(
                    DatasetType.DISTRIBUTION_LEK,
                    FileType.CSV,
                    Regex("^LEK13_(\\d{4})(\\d{2})v\\d{2}\\.csv$", RegexOption.IGNORE_CASE)
                ),
                MultiRegexLinkParser.Entry(
                    DatasetType.DISTRIBUTION_LEK,
                    FileType.ZIP,
                    Regex("^LEK13_(\\d{4})\\.zip$", RegexOption.IGNORE_CASE)
                )
            )
        )

    @Bean
    fun ereceptLinkParser(): LinkParser =
        MultiRegexLinkParser(
            listOf(
                MultiRegexLinkParser.Entry(
                    DatasetType.ERECEPT_PRESCRIPTION,
                    FileType.ZIP,
                    Regex("^erecept_predpis_(\\d{4})(\\d{2})\\.zip$", RegexOption.IGNORE_CASE)
                ),
                MultiRegexLinkParser.Entry(
                    DatasetType.ERECEPT_DISPENSE,
                    FileType.ZIP,
                    Regex("^erecept_vydej_(\\d{4})(\\d{2})\\.zip$", RegexOption.IGNORE_CASE)
                )
            )
        )

    @Bean
    fun ereceptHistoryLinkParser(): LinkParser =
        MultiRegexLinkParser(
            listOf(
                MultiRegexLinkParser.Entry(
                    DatasetType.ERECEPT_PRESCRIPTION,
                    FileType.ZIP,
                    Regex("^erecept_predpis_(\\d{4})\\.zip$", RegexOption.IGNORE_CASE)
                ),
                MultiRegexLinkParser.Entry(
                    DatasetType.ERECEPT_DISPENSE,
                    FileType.ZIP,
                    Regex("^erecept_vydej_(\\d{4})\\.zip$", RegexOption.IGNORE_CASE)
                )
            )
        )

    @Bean
    fun mpdLinkParser(): LinkParser = MpdLinkParser()

    @Bean
    fun mpdHistoryLinkParser(): LinkParser =
        RegexLinkParser(
            DatasetType.MPD,
            FileType.ZIP,
            Regex("^DLP(\\d{4})\\.zip$", RegexOption.IGNORE_CASE)
        )
}
