package cz.machovec.lekovyportal.scraper2_0.parsing

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import org.springframework.stereotype.Component

@Component
class Lek13LinkParser : LinkParser {

    companion object {
        private val MONTHLY_FILE_TYPE = FileType.CSV
        private val YEARLY_FILE_TYPE = FileType.ZIP

        private val MONTHLY_REGEX = Regex("^LEK13_(\\d{4})(\\d{2})v\\d{2}${Regex.escape(MONTHLY_FILE_TYPE.extension)}$", RegexOption.IGNORE_CASE)
        private val YEARLY_REGEX = Regex("^LEK13_(\\d{4})${Regex.escape(YEARLY_FILE_TYPE.extension)}$", RegexOption.IGNORE_CASE)
    }

    override fun parse(fileUrl: String): ParsedFileInfo? {
        val fileName = fileUrl.substringAfterLast("/")

        // Match for monthly file
        MONTHLY_REGEX.matchEntire(fileName)?.let { match ->
            val year = match.groupValues[1].toIntOrNull() ?: return null
            val month = match.groupValues[2].toIntOrNull() ?: return null

            return ParsedFileInfo(
                datasetType = DatasetType.DISTRIBUCE_LEK,
                fileType = MONTHLY_FILE_TYPE,
                year = year,
                month = month
            )
        }

        // Match for yearly file
        YEARLY_REGEX.matchEntire(fileName)?.let { match ->
            val year = match.groupValues[1].toIntOrNull() ?: return null

            return ParsedFileInfo(
                datasetType = DatasetType.DISTRIBUCE_LEK,
                fileType = YEARLY_FILE_TYPE,
                year = year,
                month = null
            )
        }

        return null
    }
}
