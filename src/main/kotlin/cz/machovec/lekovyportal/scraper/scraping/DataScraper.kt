package cz.machovec.lekovyportal.scraper.scraping

import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.NewFileMessage
import cz.machovec.lekovyportal.scraper.DatasetMeta
import cz.machovec.lekovyportal.scraper.parsing.ParsedFileInfo
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class DataScraper(
    private val htmlScraper: HtmlScraper,
    private val processedDatasetRepository: ProcessedDatasetRepository,
    private val metas: List<DatasetMeta>
) {

    fun collectNewMessages(): List<NewFileMessage> {
        val now = LocalDate.now()
        val result = mutableListOf<NewFileMessage>()

        metas.forEach { meta ->
            val links = meta.linkSuffixes.flatMap { suffix ->
                htmlScraper.scrapeLinks(meta.pageUrl, meta.linkPrefix, suffix)
            }
            links.forEach { link ->
                val info = meta.linkParser.parse(link) ?: return@forEach
                if (shouldProcess(info, now)) {
                    result += NewFileMessage(
                        datasetType = info.datasetType,
                        fileType = info.fileType,
                        year = info.year,
                        month = info.month,
                        fileUrl = link
                    )
                }
            }
        }
        return result.sortedBy { it.year * 100 + (it.month ?: 0) }
    }

    private fun shouldProcess(info: ParsedFileInfo, now: LocalDate): Boolean {
        if (info.month != null) {
            return !processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(
                info.datasetType, info.year, info.month
            )
        }
        val lastMonth = when {
            info.year < now.year -> 12
            info.year == now.year -> (now.monthValue - 1).coerceAtLeast(0)
            else -> 0
        }
        if (lastMonth == 0) return false
        for (m in 1..lastMonth) {
            if (!processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(
                    info.datasetType, info.year, m
                )
            ) return true
        }
        return false
    }
}
