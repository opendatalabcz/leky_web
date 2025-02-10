package cz.machovec.lekovyportal.scraper2_0.parsing

import cz.machovec.lekovyportal.domain.entity.DatasetType

interface LinkParser {
    fun parse(fileUrl: String): ParsedFileInfo?
}

data class ParsedFileInfo(
    val datasetType: DatasetType,
    val year: Int,
    val month: Int? = null
)
