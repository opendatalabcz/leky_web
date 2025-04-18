package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.messaging.DatasetToProcessMessage

interface DatasetFileProcessor {
    fun processFile(msg: DatasetToProcessMessage)
}
