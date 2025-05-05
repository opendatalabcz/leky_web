package cz.machovec.lekovyportal.processor.processing

import cz.machovec.lekovyportal.messaging.dto.DatasetToProcessMessage

interface DatasetProcessor {
    fun processFile(msg: DatasetToProcessMessage)
}
