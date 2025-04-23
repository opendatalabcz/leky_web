package cz.machovec.lekovyportal.scraper

import cz.machovec.lekovyportal.MpdDatasetValidityResolver
import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.messaging.DatasetToProcessMessage
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URL
import java.time.LocalDate

@Service
class DatasetDiscoveryServiceImpl(
    private val htmlScraper: HtmlScraper,
    private val processedDatasetRepository: ProcessedDatasetRepository,
    private val datasetSources: MutableList<DatasetSource>,
    private val datasetValidityResolver: MpdDatasetValidityResolver,
) : DatasetDiscoveryService {

    private val logger = KotlinLogging.logger {}

    override fun discoverDatasetsToProcess(): List<DatasetToProcessMessage> {
        val now = LocalDate.now()
        val datasetsToProcess = mutableListOf<DatasetToProcessMessage>()

        datasetSources.forEach { datasetSource ->
            htmlScraper.scrapeLinks(datasetSource.pageUrl).forEach { link ->
                val url = toValidHttpUrlOrNull(link) ?: return@forEach
                val candidateFileName = url.path.substringAfterLast("/")

                val matchedPattern = datasetSource.patterns.firstOrNull { pattern ->
                    pattern.regex.matches(candidateFileName)
                } ?: return@forEach

                val fileName = candidateFileName

                val discoveredDataset = if (matchedPattern.datasetType == DatasetType.MEDICINAL_PRODUCT_DATABASE) {
                    resolveMpdDatasetInfo(fileName, url, matchedPattern)
                } else {
                    resolveDatasetInfo(fileName, url, matchedPattern)
                } ?: return@forEach

                if (shouldProcess(discoveredDataset, now)) {
                    datasetsToProcess += DatasetToProcessMessage(
                        datasetType = discoveredDataset.datasetType,
                        fileType = discoveredDataset.fileType,
                        year = discoveredDataset.year,
                        month = discoveredDataset.month,
                        fileUrl = discoveredDataset.fileUrl,
                    )
                }
            }
        }

        return sortDatasets(datasetsToProcess)
    }

    private fun resolveDatasetInfo(
        fileName: String,
        fileUrl: URL,
        matchedPattern: SourcePattern
    ): DiscoveredDataset? {
        return matchedPattern.extractYearAndMonth(fileName)?.let { (year, month) ->
            DiscoveredDataset(
                datasetType = matchedPattern.datasetType,
                fileType = matchedPattern.fileType,
                year = year,
                month = month,
                fileName = fileName,
                fileUrl = fileUrl.toString()
            )
        }
    }

    private fun resolveMpdDatasetInfo(
        fileName: String,
        fileUrl: URL,
        pattern: SourcePattern
    ): DiscoveredDataset? {
        val (yearFromFileName, monthFromFileName) = pattern.extractYearAndMonth(fileName) ?: return null

        // Case 1 – Monthly dataset: the date in the filename represents the publishing date,
        // so we need to determine the actual validity date from the zip content
        if (monthFromFileName != null) {
            val datasetValidity = runCatching {
                datasetValidityResolver.resolveFromZip(fileUrl.readBytes())
            }.getOrNull()

            if (datasetValidity == null) {
                logger.warn { "Unable to determine dataset validity from $fileName – file dlp_platnost.csv is missing or malformed." }
                return null
            }

            return DiscoveredDataset(
                datasetType = pattern.datasetType,
                fileType = FileType.ZIP,
                year = datasetValidity.validFrom.year,
                month = datasetValidity.validFrom.monthValue,
                fileName = fileName,
                fileUrl = fileUrl.toString()
            )
        }

        // Case 2 – Yearly dataset: the year in the filename represents the validity period directly
        return DiscoveredDataset(
            datasetType = pattern.datasetType,
            fileType = FileType.ZIP,
            year = yearFromFileName,
            month = null,
            fileName = fileName,
            fileUrl = fileUrl.toString()
        )
    }


    private fun shouldProcess(discoveredDataset: DiscoveredDataset, now: LocalDate): Boolean {
        // Case 1 - Monthly dataset: verify if specific (year, month) was already processed
        if (discoveredDataset.month != null) {
            return !processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(
                discoveredDataset.datasetType, discoveredDataset.year, discoveredDataset.month
            )
        }

        // Case 2 - Yearly dataset - possible datasets:
        // For previous years: MEDICINAL_PRODUCT_DATABASE, DISTRIBUTIONS_FROM_PHARMACIES,
        // For previous years and also for the current year: ERECEPT_PRESCRIPTIONS, ERECEPT_DISPENSES,
        val isEreceptDataset = discoveredDataset.datasetType in setOf(
            DatasetType.ERECEPT_DISPENSES,
            DatasetType.ERECEPT_PRESCRIPTIONS
        )

        val latestExpectedMonthInYear = when {
            discoveredDataset.year < now.year -> 12
            discoveredDataset.year == now.year && isEreceptDataset -> now.monthValue - 1
            else -> return false // future dataset or non-eRecept yearly dataset in current year
        }

        return (1..latestExpectedMonthInYear).any { month ->
            !processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(
                discoveredDataset.datasetType, discoveredDataset.year, month
            )
        }
    }

    private fun toValidHttpUrlOrNull(link: String): URL? =
        runCatching {
            val uri = URI(link)
            if (uri.scheme == "http" || uri.scheme == "https") uri.toURL() else null
        }.getOrNull()

    private fun sortDatasets(datasets: List<DatasetToProcessMessage>): List<DatasetToProcessMessage> {
        val datasetTypePriority = listOf(
            DatasetType.MEDICINAL_PRODUCT_DATABASE,
            DatasetType.ERECEPT_PRESCRIPTIONS,
            DatasetType.ERECEPT_DISPENSES,
            DatasetType.DISTRIBUTIONS_FROM_MAHS,
            DatasetType.DISTRIBUTIONS_FROM_DISTRIBUTORS,
            DatasetType.DISTRIBUTIONS_EXPORT_FROM_DISTRIBUTORS,
            DatasetType.DISTRIBUTIONS_FROM_PHARMACIES
        ).withIndex().associate { it.value to it.index }

        return datasets.sortedWith(
            compareBy(
                { datasetTypePriority[it.datasetType] ?: Int.MAX_VALUE },
                { it.year },
                { it.month ?: 0 }
            )
        )
    }
}
