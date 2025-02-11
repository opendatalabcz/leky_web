package cz.machovec.lekovyportal.scraper2_0.parsing

import cz.machovec.lekovyportal.domain.entity.DatasetType
import org.springframework.stereotype.Component

@Component
class Lek13LinkParser : LinkParser {
    private val monthlyRegex = Regex("^LEK13_(\\d{4})(\\d{2})v\\d{2}\\.csv\$", RegexOption.IGNORE_CASE)
    private val yearlyRegex = Regex("^LEK13_(\\d{4})\\.zip\$", RegexOption.IGNORE_CASE)

    override fun parse(fileUrl: String): ParsedFileInfo? {
        val fileName = fileUrl.substringAfterLast("/")

        monthlyRegex.matchEntire(fileName)?.let { match ->
            val year = match.groupValues[1].toIntOrNull() ?: return null
            val month = match.groupValues[2].toIntOrNull() ?: return null

            return ParsedFileInfo(
                datasetType = DatasetType.DISTRIBUCE_LEK,
                year = year,
                month = month
            )
        }

        yearlyRegex.matchEntire(fileName)?.let { match ->
            val year = match.groupValues[1].toIntOrNull() ?: return null

            return ParsedFileInfo(
                datasetType = DatasetType.DISTRIBUCE_LEK,
                year = year,
                month = null
            )
        }

        return null
    }
}
