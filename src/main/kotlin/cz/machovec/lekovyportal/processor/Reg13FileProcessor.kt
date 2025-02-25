package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.messaging.NewFileMessage
import org.springframework.stereotype.Service

@Service
class Reg13FileProcessor: DatasetFileProcessor {

    override fun processFile(msg: NewFileMessage) {
        println("Reg13 processing: $msg")
    }
}
