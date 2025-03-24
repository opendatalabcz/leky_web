package cz.machovec.lekovyportal.scraper.parsing

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import org.springframework.stereotype.Component

@Component
class EreceptLinkParser : LinkParser {

    companion object {
        private val FILE_TYPE = FileType.ZIP
        private val PREDPIS_REGEX = Regex("^erecept_predpis_(\\d{4})(\\d{2})${Regex.escape(FILE_TYPE.extension)}$", RegexOption.IGNORE_CASE)
        private val VYDEJ_REGEX = Regex("^erecept_vydej_(\\d{4})(\\d{2})${Regex.escape(FILE_TYPE.extension)}$", RegexOption.IGNORE_CASE)
    }

    override fun parse(fileUrl: String): ParsedFileInfo? {
        val fileName = fileUrl.substringAfterLast("/")

        PREDPIS_REGEX.matchEntire(fileName)?.let { match ->
            val year = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            return ParsedFileInfo(
                datasetType = DatasetType.ERECEPT_PRESCRIPTION,
                fileType = FILE_TYPE,
                year = year,
                month = month
            )
        }

        VYDEJ_REGEX.matchEntire(fileName)?.let { match ->
            val year = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            return ParsedFileInfo(
                datasetType = DatasetType.ERECEPT_DISPENSE,
                fileType = FILE_TYPE,
                year = year,
                month = month
            )
        }

        return null
    }
}
