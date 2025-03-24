package cz.machovec.lekovyportal.scraper.parsing

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import org.springframework.stereotype.Component

@Component
class Dis13ZahraniciLinkParser : LinkParser {

    companion object {
        private val FILE_TYPE = FileType.CSV
        private val REGEX = Regex("^DIS13_ZAHRANICI_(\\d{4})(\\d{2})v\\d{2}${Regex.escape(FILE_TYPE.extension)}$", RegexOption.IGNORE_CASE)
    }

    override fun parse(fileUrl: String): ParsedFileInfo? {
        val fileName = fileUrl.substringAfterLast("/")
        val match = REGEX.matchEntire(fileName) ?: return null

        val year = match.groupValues[1].toIntOrNull() ?: return null
        val month = match.groupValues[2].toIntOrNull() ?: return null

        return ParsedFileInfo(
            datasetType = DatasetType.DISTRIBUTION_DIS_ABROAD,
            fileType = FILE_TYPE,
            year = year,
            month = month
        )
    }
}
