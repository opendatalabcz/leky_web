package cz.machovec.lekovyportal.scraper.model

data class DatasetSource(
    val pageUrl: String,
    val patterns: List<DatasetSourcePattern>
)
