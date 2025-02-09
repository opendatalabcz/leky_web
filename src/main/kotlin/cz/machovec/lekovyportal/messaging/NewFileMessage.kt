package cz.machovec.lekovyportal.messaging

import cz.machovec.lekovyportal.domain.entity.DatasetType

data class NewFileMessage(
    val datasetType: DatasetType,
    val year: Int,
    val month: Int,
    val fileUrl: String
)