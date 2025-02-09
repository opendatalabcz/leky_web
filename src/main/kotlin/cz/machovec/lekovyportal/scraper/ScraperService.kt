package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.messaging.MessagePublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScraperService(
    private val reg13Scraper: Reg13Scraper,
    private val messagePublisher: MessagePublisher
) {
    @Scheduled(cron = "0 * * * * ?")
    fun crawlReg13() {
        val newMessages = reg13Scraper.scrape()

        newMessages.forEach { msg ->
            messagePublisher.publish(msg)
        }
    }
}