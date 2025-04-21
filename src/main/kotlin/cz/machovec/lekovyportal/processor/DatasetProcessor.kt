package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.messaging.DatasetToProcessMessage

interface DatasetProcessor {
    fun processFile(msg: DatasetToProcessMessage)
}
