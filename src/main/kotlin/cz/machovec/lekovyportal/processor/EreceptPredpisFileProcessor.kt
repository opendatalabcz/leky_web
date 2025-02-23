package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.messaging.NewFileMessage
import org.springframework.stereotype.Service

@Service
class EreceptPredpisFileProcessor: DatasetFileProcessor {

    override fun processFile(msg: NewFileMessage) {
        println("Erecept predpis processing: $msg")
    }
}
