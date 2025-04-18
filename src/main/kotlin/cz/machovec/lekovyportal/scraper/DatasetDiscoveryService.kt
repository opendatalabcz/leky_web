package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.messaging.DatasetToProcessMessage

interface DatasetDiscoveryService {
    fun discoverDatasetsToProcess(): List<DatasetToProcessMessage>
}
