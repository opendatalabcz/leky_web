package cz.machovec.lekovyportal.messaging.dto

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.core.domain.dataset.FileType

data class DatasetToProcessMessage(
    val datasetType: DatasetType,
    val fileType: FileType,
    val year: Int,
    val month: Int?,
    val fileUrl: String
)
