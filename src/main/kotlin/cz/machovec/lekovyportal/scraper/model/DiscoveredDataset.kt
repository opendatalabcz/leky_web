package cz.machovec.lekovyportal.scraper.model

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.core.domain.dataset.FileType

data class DiscoveredDataset(
    val datasetType: DatasetType,
    val fileType: FileType,
    val year: Int,
    val month: Int?,
    val fileName: String,
    val fileUrl: String
)
