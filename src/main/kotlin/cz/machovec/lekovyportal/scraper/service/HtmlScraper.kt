package cz.machovec.lekovyportal.scraper.service

interface HtmlScraper {
    fun scrapeLinks(url: String): List<String>
}