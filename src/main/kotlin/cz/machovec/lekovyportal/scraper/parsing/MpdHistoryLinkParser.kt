package cz.machovec.lekovyportal.scraper.parsing

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import org.springframework.stereotype.Component

@Component
class MpdHistoryLinkParser : LinkParser {

    companion object {
        private val FILE_TYPE = FileType.ZIP
        private val YEAR_MONTH_REGEX = Regex("^DLP(\\d{4})(\\d{2})${Regex.escape(FILE_TYPE.extension)}$", RegexOption.IGNORE_CASE)
        private val YEAR_REGEX = Regex("^DLP(\\d{4})${Regex.escape(FILE_TYPE.extension)}$", RegexOption.IGNORE_CASE)
    }

    override fun parse(fileUrl: String): ParsedFileInfo? {
        val fileName = fileUrl.substringAfterLast("/")

        YEAR_MONTH_REGEX.matchEntire(fileName)?.let { match ->
            val year = match.groupValues[1].toIntOrNull() ?: return null
            val month = match.groupValues[2].toIntOrNull() ?: return null
            return ParsedFileInfo(
                datasetType = DatasetType.MPD,
                fileType = FILE_TYPE,
                year = year,
                month = month
            )
        }

        YEAR_REGEX.matchEntire(fileName)?.let { match ->
            val year = match.groupValues[1].toIntOrNull() ?: return null
            return ParsedFileInfo(
                datasetType = DatasetType.MPD,
                fileType = FILE_TYPE,
                year = year,
                month = null
            )
        }

        return null
    }
}
