package cz.machovec.lekovyportal.messaging

interface MessagePublisher {
    fun publish(msg: DatasetToProcessMessage)
}