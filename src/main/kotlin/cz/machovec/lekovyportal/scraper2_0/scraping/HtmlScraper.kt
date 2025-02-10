package cz.machovec.lekovyportal.scraper2_0.scraping

interface HtmlScraper {
    fun scrapeLinks(url: String, prefix: String, suffix: String): List<String>
}