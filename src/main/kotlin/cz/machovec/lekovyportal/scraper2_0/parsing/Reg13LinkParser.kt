package cz.machovec.lekovyportal.scraper2_0.parsing

import cz.machovec.lekovyportal.domain.entity.DatasetType
import org.springframework.stereotype.Component

@Component
class Reg13LinkParser : LinkParser {
    private val regex = Regex("^REG13_(\\d{4})(\\d{2})v\\d{2}\\.csv\$", RegexOption.IGNORE_CASE)

    override fun parse(fileUrl: String): ParsedFileInfo? {
        val fileName = fileUrl.substringAfterLast("/")
        val match = regex.matchEntire(fileName) ?: return null

        val year = match.groupValues[1].toIntOrNull() ?: return null
        val month = match.groupValues[2].toIntOrNull() ?: return null

        return ParsedFileInfo(
            datasetType = DatasetType.DISTRIBUCE_REG,
            year = year,
            month = month
        )
    }
}
