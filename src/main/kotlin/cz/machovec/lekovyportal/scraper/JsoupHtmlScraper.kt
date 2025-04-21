package cz.machovec.lekovyportal.scraper

import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class JsoupHtmlScraper : HtmlScraper {
    override fun scrapeLinks(url: String): List<String> {
        val doc = Jsoup.connect(url).get()
        return doc.select("a[href]").map { it.attr("abs:href") }
    }
}
