package cz.machovec.lekovyportal.messaging

import cz.machovec.lekovyportal.domain.entity.DatasetType
import java.io.Serializable

data class NewFileMessage(
    val datasetType: DatasetType,
    val year: Int,
    val month: Int?,
    val fileUrl: String
) : Serializable
