package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.messaging.DatasetToProcessMessage
import cz.machovec.lekovyportal.messaging.RabbitConfig
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class DatasetToProcessConsumer(
    private val resolver: DatasetProcessorResolver
) {
    @RabbitListener(queues = [RabbitConfig.QUEUE_NAME])
    fun handleNewFile(msg: DatasetToProcessMessage) {
        val processor = resolver.resolve(msg.datasetType)
        if (processor == null) {
            println("No processor found for datasetType=${msg.datasetType}")
            return
        }
        processor.processFile(msg)
    }
}
