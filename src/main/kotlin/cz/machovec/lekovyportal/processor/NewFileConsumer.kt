package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.messaging.NewFileMessage
import cz.machovec.lekovyportal.messaging.RabbitConfig
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class NewFileConsumer(
    private val resolver: FileProcessorResolver
) {
    @RabbitListener(queues = [RabbitConfig.QUEUE_NAME])
    fun handleNewFile(msg: NewFileMessage) {
        val processor = resolver.resolve(msg.datasetType)
        if (processor == null) {
            println("No processor found for datasetType=${msg.datasetType}")
            return
        }
        processor.processFile(msg)
    }
}
