package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.messaging.MessagePublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScraperScheduler(
    private val reg13Service: Reg13Service,
    private val dis13Service: Dis13Service,
    private val dis13ZahraniciService: Dis13ZahraniciService,
    private val ereceptService: EreceptService,
    private val ereceptHistoryService: EreceptHistoryService,
    private val messagePublisher: MessagePublisher,
    private val lek13Service: Lek13Service,
    private val mpdService: MpdService,
    private val mpdHistoryService: MpdHistoryService,
) {
    @Scheduled(cron = "0 * * * * ?")
    fun doScraping() {

        reg13Service.collectNewMessages().forEach { messagePublisher.publish(it) }

        dis13Service.collectNewMessages().forEach { messagePublisher.publish(it) }

        dis13ZahraniciService.collectNewMessages().forEach { messagePublisher.publish(it) }

        lek13Service.collectNewMessages().forEach { messagePublisher.publish(it) }

        ereceptHistoryService.collectNewMessages().forEach { messagePublisher.publish(it) }

        ereceptService.collectNewMessages().forEach { messagePublisher.publish(it) }

        mpdService.collectNewMessages().forEach { messagePublisher.publish(it) }

        mpdHistoryService.collectNewMessages().forEach { messagePublisher.publish(it) }
    }
}
