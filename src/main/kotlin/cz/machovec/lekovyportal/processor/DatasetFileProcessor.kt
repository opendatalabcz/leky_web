package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.messaging.NewFileMessage

interface DatasetFileProcessor {
    fun processFile(msg: NewFileMessage)
}
