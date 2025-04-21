package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.DatasetToProcessMessage
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream

@Service
class DatasetDiscoveryServiceImpl(
    private val htmlScraper: HtmlScraper,
    private val processedDatasetRepository: ProcessedDatasetRepository,
    private val datasetSources: List<DatasetSource>
) : DatasetDiscoveryService {

    override fun discoverDatasetsToProcess(): List<DatasetToProcessMessage> {
        val now = LocalDate.now()
        val results = mutableListOf<DatasetToProcessMessage>()

        datasetSources.forEach { source ->
            htmlScraper.scrapeLinks(source.pageUrl).forEach { link ->
                val name = link.substringAfterLast("/")

                val pattern = source.patterns.firstOrNull { it.regex.matches(name) } ?: return@forEach
                val info = if (pattern.datasetType == DatasetType.MEDICINAL_PRODUCT_DATABASE) {
                    parseMpd(link, name, pattern)
                } else {
                    parseGeneric(name, pattern)
                } ?: return@forEach

                if (shouldProcess(info, now)) {
                    results += DatasetToProcessMessage(
                        datasetType = info.datasetType,
                        fileType = info.fileType,
                        year = info.year,
                        month = info.month,
                        fileUrl = link
                    )
                }
            }
        }

        return results.sortedBy { it.year * 100 + (it.month ?: 0) }
    }

    private fun parseGeneric(fileName: String, pattern: SourcePattern): ParsedDatasetInfo? {
        val m = pattern.regex.matchEntire(fileName) ?: return null
        val year = m.groupValues[1].toInt()
        val month = m.groupValues.getOrNull(2)?.toIntOrNull()
        return ParsedDatasetInfo(pattern.datasetType, pattern.fileType, year, month)
    }

    private fun parseMpd(
        fileUrl: String,
        fileName: String,
        pattern: SourcePattern
    ): ParsedDatasetInfo? {
        val match = pattern.regex.matchEntire(fileName) ?: return null
        val year  = match.groupValues[1].toInt()
        val monthFromName = match.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() }?.toInt()

        if (monthFromName != null) {
            val period = runCatching {
                determineMpdPeriod(URL(fileUrl).openStream().readBytes())
            }.getOrNull()

            val (resolvedYear, resolvedMonth) = period ?: Pair(year, monthFromName)
            return ParsedDatasetInfo(pattern.datasetType, FileType.ZIP, resolvedYear, resolvedMonth)
        }

        return ParsedDatasetInfo(pattern.datasetType, FileType.ZIP, year, null)
    }

    private fun determineMpdPeriod(zipBytes: ByteArray): Pair<Int, Int>? {
        ZipInputStream(zipBytes.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name == "dlp_platnost.csv") {
                    BufferedReader(InputStreamReader(zis)).use { br ->
                        br.readLine()
                        val line = br.readLine() ?: return null
                        val date = LocalDate.parse(
                            line.split(";")[0],
                            DateTimeFormatter.ofPattern("dd.MM.yyyy")
                        )
                        return Pair(date.year, date.monthValue)
                    }
                }
                entry = zis.nextEntry
            }
        }
        return null
    }

    private fun shouldProcess(info: ParsedDatasetInfo, now: LocalDate): Boolean {
        if (info.month != null) {
            return !processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(info.datasetType, info.year, info.month)
        }

        val lastMonth = when {
            info.year < now.year -> 12
            info.year == now.year -> (now.monthValue - 1).coerceAtLeast(0)
            else -> return false
        }
        if (lastMonth == 0) return false

        for (m in 1..lastMonth) {
            if (!processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(info.datasetType, info.year, m)) {
                return true
            }
        }
        return false
    }
}
