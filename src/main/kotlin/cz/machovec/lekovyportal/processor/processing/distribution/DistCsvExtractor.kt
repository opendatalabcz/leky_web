package cz.machovec.lekovyportal.processor.processing.distribution

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.core.domain.dataset.FileType
import cz.machovec.lekovyportal.core.util.ZipFileUtils
import org.springframework.stereotype.Component

@Component
class DistCsvExtractor {

    companion object {
        private val PHARMACY_CSV_FILENAME_MONTH_REGEX = Regex("""LEK13_\d{4}(\d{2})v\d+\.csv""")
        private val ZIP_SUPPORTED_DATASETS = listOf(DatasetType.DISTRIBUTIONS_FROM_PHARMACIES)
    }

    /**
     * Extracts distribution CSV files grouped by month, based on the given [fileType].
     *
     * - For [FileType.CSV], the method expects a single CSV representing one [month].
     * - For [FileType.ZIP], the method expects multiple monthly CSVs bundled (supported only for selected datasets).
     *
     * Throws if inputs don't match expectations.
     */
    fun extractCsvFilesByMonth(
        fileBytes: ByteArray,
        month: Int?,
        fileType: FileType,
        datasetType: DatasetType
    ): Map<Int, ByteArray> {
        return when (fileType) {
            FileType.CSV -> {
                requireNotNull(month) {
                    "DatasetType $datasetType expects a monthly CSV file, but no month was provided."
                }
                mapOf(month to fileBytes)
            }

            FileType.ZIP -> {
                require(month == null) {
                    "DatasetType $datasetType expects a yearly ZIP file, but a month ($month) was provided."
                }

                require(datasetType in ZIP_SUPPORTED_DATASETS) {
                    "DatasetType $datasetType does not support yearly ZIP processing."
                }

                extractMonthlyCsvsFromZip(fileBytes) { fileName ->
                    PHARMACY_CSV_FILENAME_MONTH_REGEX.matchEntire(fileName)
                        ?.groups?.get(1)?.value?.toIntOrNull()
                }
            }
        }
    }

    /**
     * Extracts CSV files from a ZIP archive and groups them by month using [monthExtractor].
     */
    private fun extractMonthlyCsvsFromZip(
        fileBytes: ByteArray,
        monthExtractor: (String) -> Int?
    ): Map<Int, ByteArray> =
        ZipFileUtils.extractFilesByType(fileBytes, FileType.CSV)
            .mapNotNull { (fileName, csvBytes) ->
                val month = monthExtractor(fileName)
                if (month != null) month to csvBytes else null
            }
            .toMap()
}
