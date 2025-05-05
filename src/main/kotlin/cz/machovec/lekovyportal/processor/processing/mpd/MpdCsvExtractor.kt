package cz.machovec.lekovyportal.processor.processing.mpd

import cz.machovec.lekovyportal.core.domain.dataset.FileType
import cz.machovec.lekovyportal.core.domain.mpd.MpdDatasetType
import cz.machovec.lekovyportal.core.util.ZipFileUtils
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class MpdCsvExtractor {

    companion object {
        private val NESTED_ZIP_MONTH_REGEX = Regex(""".*(\d{4})(\d{2})\.zip""", RegexOption.IGNORE_CASE)

        /**
         * Extracts month (MM) from nested ZIP file name in format like `MPD_202304.zip`.
         */
        fun extractMonthFromZipName(name: String): Int? =
            NESTED_ZIP_MONTH_REGEX.matchEntire(name)?.groupValues?.get(2)?.toIntOrNull()
    }

    private val log = KotlinLogging.logger {}

    /**
     * Extracts MPD CSV files grouped by month from a ZIP archive.
     *
     * If [month] is provided, the ZIP is treated as a single-month ZIP and all CSVs are parsed from it.
     * If [month] is null, the ZIP is assumed to contain nested ZIPs (one per month),
     * and each nested ZIP must follow a strict `*.yyyyMM.zip` naming format.
     *
     * Throws an exception if a nested ZIP cannot be assigned to a month or if duplicates are found.
     */
    fun extractMonthlyCsvFilesFromZip(
        zipBytes: ByteArray,
        month: Int?
    ): Map<Int, Map<MpdDatasetType, ByteArray>> {
        return if (month == null) {
            extractMonthlyZipsFromAnnualZip(zipBytes)
        } else {
            mapOf(month to extractMpdCsvFiles(zipBytes))
        }
    }

    /**
     * Extracts all nested monthly ZIP files from an annual ZIP archive,
     * maps them by month, and parses their contents into [MpdDatasetType]-keyed maps.
     *
     * Fails if any ZIP name does not match the expected format or if duplicate months are found.
     */
    private fun extractMonthlyZipsFromAnnualZip(annualZip: ByteArray): Map<Int, Map<MpdDatasetType, ByteArray>> {
        val grouped = mutableMapOf<Int, Map<MpdDatasetType, ByteArray>>()

        ZipFileUtils.extractFilesByType(annualZip, FileType.ZIP).forEach { (zipName, zipBytes) ->
            val month = extractMonthFromZipName(zipName)
                ?: error("Cannot determine month from nested ZIP name: $zipName")

            if (grouped.putIfAbsent(month, extractMpdCsvFiles(zipBytes)) != null) {
                error("Duplicate nested ZIP for month=$month in $zipName")
            }
        }

        return grouped.toSortedMap()
    }

    /**
     * Extracts CSV files from a ZIP archive and maps them by [MpdDatasetType].
     *
     * Ignores files that do not match any known dataset type.
     */
    private fun extractMpdCsvFiles(zipBytes: ByteArray): Map<MpdDatasetType, ByteArray> {
        return ZipFileUtils.extractFilesByType(zipBytes, FileType.CSV)
            .mapNotNull { (fileName, content) ->
                MpdDatasetType.fromFileName(fileName)?.let { it to content }
                    ?: run {
                        log.debug { "Unknown CSV file: $fileName" }
                        null
                    }
            }.toMap()
    }
}
