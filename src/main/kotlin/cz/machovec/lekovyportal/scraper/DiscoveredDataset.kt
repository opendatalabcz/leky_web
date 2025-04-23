package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType

data class DiscoveredDataset(
    val datasetType: DatasetType,
    val fileType: FileType,
    val year: Int,
    val month: Int?,
    val fileName: String,
    val fileUrl: String
)
