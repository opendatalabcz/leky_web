package cz.machovec.lekovyportal.scraper2_0.parsing

import cz.machovec.lekovyportal.domain.entity.DatasetType
import org.springframework.stereotype.Component

@Component
class EreceptLinkParser : LinkParser {
    private val predpisRegex = Regex("^erecept_predpis_(\\d{4})(\\d{2})\\.zip$", RegexOption.IGNORE_CASE)
    private val vydejRegex   = Regex("^erecept_vydej_(\\d{4})(\\d{2})\\.zip$", RegexOption.IGNORE_CASE)

    override fun parse(fileUrl: String): ParsedFileInfo? {
        val fileName = fileUrl.substringAfterLast("/")

        predpisRegex.matchEntire(fileName)?.let {
            val year = it.groupValues[1].toInt()
            val month = it.groupValues[2].toInt()
            return ParsedFileInfo(DatasetType.ERECEPT_PREDPIS, year, month)
        }
        vydejRegex.matchEntire(fileName)?.let {
            val year = it.groupValues[1].toInt()
            val month = it.groupValues[2].toInt()
            return ParsedFileInfo(DatasetType.ERECEPT_VYDEJ, year, month)
        }

        return null
    }
}

