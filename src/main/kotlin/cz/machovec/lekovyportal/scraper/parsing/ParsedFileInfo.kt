package cz.machovec.lekovyportal.scraper.parsing

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType

data class ParsedFileInfo(
    val datasetType: DatasetType,
    val fileType: FileType,
    val year: Int,
    val month: Int?
)
