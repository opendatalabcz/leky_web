package cz.machovec.lekovyportal.scraper.scraping

interface HtmlScraper {
    fun scrapeLinks(url: String, prefix: String, suffix: String): List<String>
}