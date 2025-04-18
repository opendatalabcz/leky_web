package cz.machovec.lekovyportal.messaging

import mu.KotlinLogging
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class RabbitMessagePublisher(
    private val rabbitTemplate: RabbitTemplate
) : MessagePublisher {
    override fun publish(msg: NewFileMessage) {
        logger.info { "Publishing message to RabbitMQ: datasetType=${msg.datasetType}, year=${msg.year}, month=${msg.month}, fileType=${msg.fileType}" }

        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE_NAME,
            RabbitConfig.ROUTING_KEY,
            msg
        )
    }
}