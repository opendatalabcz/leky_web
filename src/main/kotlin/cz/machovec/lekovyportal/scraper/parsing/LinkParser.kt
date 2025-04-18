package cz.machovec.lekovyportal.scraper.parsing

interface LinkParser {
    fun parse(fileUrl: String): ParsedFileInfo?
}
