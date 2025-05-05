package cz.machovec.lekovyportal.messaging.port

import cz.machovec.lekovyportal.messaging.dto.DatasetToProcessMessage

interface MessagePublisher {
    fun publish(msg: DatasetToProcessMessage)
}