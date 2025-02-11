package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.messaging.NewFileMessage
import cz.machovec.lekovyportal.messaging.RabbitConfig
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class NewFileConsumer {
    @RabbitListener(queues = [RabbitConfig.QUEUE_NAME])
    fun handleNewFileMessage(msg: NewFileMessage) {
        println("Received from RabbitMQ: $msg")
    }
}
