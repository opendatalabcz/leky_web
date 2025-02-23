package cz.machovec.lekovyportal.messaging

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class RabbitMessagePublisher(
    private val rabbitTemplate: RabbitTemplate
) : MessagePublisher {
    override fun publish(msg: NewFileMessage) {
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE_NAME,
            RabbitConfig.ROUTING_KEY,
            msg
        )
    }
}