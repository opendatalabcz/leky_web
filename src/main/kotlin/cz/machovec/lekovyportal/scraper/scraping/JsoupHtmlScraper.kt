package cz.machovec.lekovyportal.scraper.scraping

import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class JsoupHtmlScraper : HtmlScraper {
    override fun scrapeLinks(url: String, prefix: String, suffix: String): List<String> {
        val doc = Jsoup.connect(url).get()

        val allLinks = doc.select("a[href]").map { it.attr("abs:href") }

        val filtered = allLinks.filter { link ->
            val passPrefix = link.startsWith(prefix, ignoreCase = true)
            val passSuffix = link.endsWith(suffix, ignoreCase = true)
            passPrefix && passSuffix
        }

        return filtered
    }
}