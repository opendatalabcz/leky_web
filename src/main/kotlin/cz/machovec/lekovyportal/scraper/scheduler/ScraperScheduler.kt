package cz.machovec.lekovyportal.scraper.scheduler

import cz.machovec.lekovyportal.messaging.port.MessagePublisher
import cz.machovec.lekovyportal.scraper.service.DatasetDiscoveryService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScraperScheduler(
    private val datasetDiscoveryService: DatasetDiscoveryService,
    private val messagePublisher: MessagePublisher
) {
    @Scheduled(cron = "0 0 0 * * ?")
    fun doScraping() {
        datasetDiscoveryService.discoverDatasetsToProcess().forEach(messagePublisher::publish)
    }
}
