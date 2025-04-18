package cz.machovec.lekovyportal.scraper

interface HtmlScraper {
    fun scrapeLinks(url: String): List<String>
}