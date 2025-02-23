package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.messaging.NewFileMessage
import org.springframework.stereotype.Service

@Service
class EreceptVydejFileProcessor: DatasetFileProcessor {

    override fun processFile(msg: NewFileMessage) {
        println("Erecept vydej processing: $msg")
    }
}
