package cz.machovec.lekovyportal.messaging

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType

data class DatasetToProcessMessage(
    val datasetType: DatasetType,
    val fileType: FileType,
    val year: Int,
    val month: Int?,
    val fileUrl: String
)
