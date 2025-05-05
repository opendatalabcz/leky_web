package cz.machovec.lekovyportal.scraper.model

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.core.domain.dataset.FileType

data class DatasetSourcePattern(
    val regex: Regex,
    val fileType: FileType,
    val datasetType: DatasetType,
    val yearGroupIndex: Int,
    val monthGroupIndex: Int? = null
) {
    fun extractYearAndMonth(fileName: String): Pair<Int, Int?>? {
        val match = regex.matchEntire(fileName) ?: return null
        val year = match.groupValues.getOrNull(yearGroupIndex)?.toIntOrNull() ?: return null
        val month = monthGroupIndex?.let { idx ->
            match.groupValues.getOrNull(idx)?.toIntOrNull()
        }
        return year to month
    }
}