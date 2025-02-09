package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.messaging.MessagePublisher
import org.jsoup.Jsoup
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScraperService(
    private val reg13Scraper: Reg13Scraper,
    private val ereceptScraper: EreceptScraper,
    private val ereceptHistoryScraper: EreceptHistoryScraper,
    private val messagePublisher: MessagePublisher
) {
    private val PREFIX_OF_SEARCHED_FILES = "https://opendata.sukl.cz/soubory/"

    private val URLS_TO_CRAWL = listOf(
        "https://opendata.sukl.cz/?q=katalog/predepsane-vydane-lecive-pripravky-ze-systemu-erecept",
        "https://opendata.sukl.cz/?q=katalog/historie-predepsanych-vydanych-lecivych-pripravku-ze-systemu-erecept",
        "https://opendata.sukl.cz/?q=katalog/databaze-lecivych-pripravku-dlp",
        "https://opendata.sukl.cz/?q=katalog/historie-databaze-lecivych-pripravku-dlp",
        "https://opendata.sukl.cz/?q=katalog/hlaseni-o-uvedeni-preruseni-ukonceni-obnoveni-dodavek-leciveho-pripravku-na-trh",
        "https://opendata.sukl.cz/?q=katalog/lek-13",
        "https://opendata.sukl.cz/?q=katalog/dis-13",
        "https://opendata.sukl.cz/?q=katalog/dis-13-zahranici",
        "https://opendata.sukl.cz/?q=katalog/reg-13"
    )

    @Scheduled(cron = "0 * * * * ?")
    fun scrape() {
        scrapeAll()

        // scrapePOC()
    }

    private fun scrapeAll() {
        val newReg13 = reg13Scraper.scrape()
        newReg13.forEach { messagePublisher.publish(it) }

        val newEreceptHistory = ereceptHistoryScraper.scrape()
        newEreceptHistory.forEach { messagePublisher.publish(it) }

        val newErecept = ereceptScraper.scrape()
        newErecept.forEach { messagePublisher.publish(it) }
    }

    private fun scrapePOC() {
        println("Spouštím crawler pro více stránek...")

        for ((index, url) in URLS_TO_CRAWL.withIndex()) {
            println("----------- Stránka #$index: $url -----------")

            try {
                val doc = Jsoup.connect(url).get()

                val links = doc.select("a[href]")
                println("Na stránce bylo nalezeno ${links.size} odkazů.")

                val relevantLinks = links.filter { link ->
                    val href = link.attr("abs:href")
                    href.startsWith(PREFIX_OF_SEARCHED_FILES)
                }

                println("Relevantní odkazy (${relevantLinks.size}):")
                for ((linkIndex, link) in relevantLinks.withIndex()) {
                    val href = link.attr("abs:href")
                    println("  #$linkIndex => $href")
                }
            } catch (e: Exception) {
                println("Chyba při scrapování stránky: $url")
            }

            println("--------------------------------------------")
        }
    }
}