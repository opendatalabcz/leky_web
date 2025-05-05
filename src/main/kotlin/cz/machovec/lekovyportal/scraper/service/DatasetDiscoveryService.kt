package cz.machovec.lekovyportal.scraper.service

import cz.machovec.lekovyportal.messaging.dto.DatasetToProcessMessage

interface DatasetDiscoveryService {
    fun discoverDatasetsToProcess(): List<DatasetToProcessMessage>
}
