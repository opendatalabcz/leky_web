package cz.machovec.lekovyportal.messaging.rabbit

import cz.machovec.lekovyportal.messaging.dto.DatasetToProcessMessage
import cz.machovec.lekovyportal.messaging.port.MessagePublisher
import mu.KotlinLogging
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class RabbitMessagePublisher(
    private val rabbitTemplate: RabbitTemplate
) : MessagePublisher {

    private val logger = KotlinLogging.logger {}

    override fun publish(msg: DatasetToProcessMessage) {
        logger.info { "Publishing message to RabbitMQ: datasetType=${msg.datasetType}, year=${msg.year}, month=${msg.month}, fileType=${msg.fileType}" }

        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE_NAME,
            RabbitConfig.ROUTING_KEY,
            msg
        )
    }
}