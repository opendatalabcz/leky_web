package cz.machovec.lekovyportal.processor.consumer

import cz.machovec.lekovyportal.processor.config.DatasetProcessorResolver
import cz.machovec.lekovyportal.messaging.dto.DatasetToProcessMessage
import cz.machovec.lekovyportal.messaging.rabbit.RabbitConfig
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class DatasetToProcessConsumer(
    private val resolver: DatasetProcessorResolver
) {

    private val logger = KotlinLogging.logger {}

    @RabbitListener(queues = [RabbitConfig.QUEUE_NAME])
    fun handleNewDatasetToProcess(msg: DatasetToProcessMessage) {
        try {
            logger.info { "Starting to process datasetType=${msg.datasetType}, year=${msg.year}, month=${msg.month}" }

            val processor = resolver.resolve(msg.datasetType)
            if (processor == null) {
                logger.error { "No processor found for datasetType=${msg.datasetType}" }
                return
            }
            processor.processFile(msg)
        } catch (ex: Exception) {
            logger.error(ex) { "Failed to process datasetType=${msg.datasetType}: ${ex.message}" }
            // Do not rethrow - continue processing next messages
        }
    }
}
