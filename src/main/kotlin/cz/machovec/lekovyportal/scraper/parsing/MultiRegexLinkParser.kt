package cz.machovec.lekovyportal.scraper.parsing

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType

class MultiRegexLinkParser(
    private val patterns: List<Entry>
) : LinkParser {

    data class Entry(
        val datasetType: DatasetType,
        val fileType: FileType,
        val regex: Regex
    )

    override fun parse(fileUrl: String): ParsedFileInfo? {
        val fileName = fileUrl.substringAfterLast("/")
        val entry = patterns.firstOrNull { it.regex.matches(fileName) } ?: return null
        val match = entry.regex.matchEntire(fileName)!!
        val year = match.groupValues[1].toInt()
        val month = match.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() }?.toInt()
        return ParsedFileInfo(entry.datasetType, entry.fileType, year, month)
    }
}
