package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.scraper.parsing.LinkParser
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ScraperMetaConfig(
    @Qualifier("dis13LinkParser") private val disDistributionLinkParser: LinkParser,
    @Qualifier("dis13ZahraniciLinkParser") private val disDistributionAbroadLinkParser: LinkParser,
    @Qualifier("reg13LinkParser") private val reg13LinkParser: LinkParser,
    @Qualifier("lek13LinkParser") private val lek13LinkParser: LinkParser,
    @Qualifier("ereceptLinkParser") private val ereceptLinkParser: LinkParser,
    @Qualifier("ereceptHistoryLinkParser") private val ereceptHistoryLinkParser: LinkParser,
    @Qualifier("mpdLinkParser") private val mpdLinkParser: LinkParser,
    @Qualifier("mpdHistoryLinkParser") private val mpdHistoryLinkParser: LinkParser
) {

    @Bean
    fun datasetMetas(): List<DatasetMeta> = listOf(
        DatasetMeta(
            pageUrl = "https://opendata.sukl.cz/?q=katalog/dis-13",
            linkPrefix = "https://opendata.sukl.cz/soubory/DIS13/",
            linkSuffixes = listOf(".csv"),
            linkParser = disDistributionLinkParser
        ),
        DatasetMeta(
            pageUrl = "https://opendata.sukl.cz/?q=katalog/dis-13-zahranici",
            linkPrefix = "https://opendata.sukl.cz/soubory/DIS13_ZAHRANICI/",
            linkSuffixes = listOf(".csv"),
            linkParser = disDistributionAbroadLinkParser
        ),
        DatasetMeta(
            pageUrl = "https://opendata.sukl.cz/?q=katalog/reg-13",
            linkPrefix = "https://opendata.sukl.cz/soubory/REG13/",
            linkSuffixes = listOf(".csv"),
            linkParser = reg13LinkParser
        ),
        DatasetMeta(
            pageUrl = "https://opendata.sukl.cz/?q=katalog/lek-13",
            linkPrefix = "https://opendata.sukl.cz/soubory/LEK13/",
            linkSuffixes = listOf(".csv", ".zip"),
            linkParser = lek13LinkParser
        ),
        DatasetMeta(
            pageUrl = "https://opendata.sukl.cz/?q=katalog/predepsane-vydane-lecive-pripravky-ze-systemu-erecept",
            linkPrefix = "https://opendata.sukl.cz/soubory/ERECEPT/",
            linkSuffixes = listOf(".zip"),
            linkParser = ereceptLinkParser
        ),
        DatasetMeta(
            pageUrl = "https://opendata.sukl.cz/?q=katalog/historie-predepsanych-vydanych-lecivych-pripravku-ze-systemu-erecept",
            linkPrefix = "https://opendata.sukl.cz/soubory/ERECEPT_HISTORIE/",
            linkSuffixes = listOf(".zip"),
            linkParser = ereceptHistoryLinkParser
        ),
        DatasetMeta(
            pageUrl = "https://opendata.sukl.cz/?q=katalog/databaze-lecivych-pripravku-dlp",
            linkPrefix = "https://opendata.sukl.cz/soubory/SOD",
            linkSuffixes = listOf(".zip"),
            linkParser = mpdLinkParser
        ),
        DatasetMeta(
            pageUrl = "https://opendata.sukl.cz/?q=katalog/historie-databaze-lecivych-pripravku-dlp",
            linkPrefix = "https://opendata.sukl.cz/soubory/SOD",
            linkSuffixes = listOf(".zip"),
            linkParser = mpdHistoryLinkParser
        )
    )
}
