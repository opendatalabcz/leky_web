package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType

data class ParsedDatasetInfo(
    val datasetType: DatasetType,
    val fileType: FileType,
    val year: Int,
    val month: Int?
)
