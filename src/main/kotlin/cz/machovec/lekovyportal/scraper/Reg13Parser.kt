package cz.machovec.lekovyportal.scraper

import org.springframework.stereotype.Component

@Component
class Reg13Parser {
    private val regex = Regex("^REG13_(\\d{4})(\\d{2})v\\d{2}\\.csv\$", RegexOption.IGNORE_CASE)

    fun parseReg13FileName(fileUrl: String): Pair<Int, Int>? {
        val fileName = fileUrl.substringAfterLast("/")
        val match = regex.matchEntire(fileName) ?: return null

        val year = match.groupValues[1].toIntOrNull() ?: return null
        val month = match.groupValues[2].toIntOrNull() ?: return null

        return Pair(year, month)
    }
}