package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.scraper.parsing.LinkParser

data class DatasetMeta(
    val pageUrl: String,
    val linkPrefix: String,
    val linkSuffixes: List<String>,
    val linkParser: LinkParser
)
