package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.messaging.MessagePublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScraperScheduler(
    private val datasetDiscoveryService: DatasetDiscoveryServiceImpl,
    private val messagePublisher: MessagePublisher
) {
    @Scheduled(cron = "0 0 0 * * ?")
    fun doScraping() {
        datasetDiscoveryService.discoverDatasetsToProcess().forEach(messagePublisher::publish)
    }
}
