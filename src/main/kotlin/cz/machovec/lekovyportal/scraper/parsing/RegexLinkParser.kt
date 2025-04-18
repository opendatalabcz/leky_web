package cz.machovec.lekovyportal.scraper.parsing

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType

class RegexLinkParser(
    private val datasetType: DatasetType,
    private val fileType: FileType,
    private val regex: Regex
) : LinkParser {

    override fun parse(fileUrl: String): ParsedFileInfo? {
        val fileName = fileUrl.substringAfterLast("/")
        val match = regex.matchEntire(fileName) ?: return null
        val year = match.groupValues[1].toInt()
        val month = match.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() }?.toInt()
        return ParsedFileInfo(datasetType, fileType, year, month)
    }
}
