package cz.machovec.lekovyportal.messaging

import org.springframework.stereotype.Service

@Service
class RabbitMessagePublisher : MessagePublisher {
    override fun publish(msg: NewFileMessage) {
        println("TODO: Sending message to rabbit -> $msg") // TODO
    }
}