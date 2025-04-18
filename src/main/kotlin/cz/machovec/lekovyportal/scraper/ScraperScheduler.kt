package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.messaging.MessagePublisher
import cz.machovec.lekovyportal.scraper.scraping.DataScraper
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScraperScheduler(
    private val dataScraper: DataScraper,
    private val messagePublisher: MessagePublisher
) {
    @Scheduled(cron = "0 * * * * ?")
    fun doScraping() {
        dataScraper.collectNewMessages().forEach(messagePublisher::publish)
    }
}
