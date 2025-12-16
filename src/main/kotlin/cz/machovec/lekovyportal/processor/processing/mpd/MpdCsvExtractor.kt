package cz.machovec.lekovyportal.processor.processing.mpd

import cz.machovec.lekovyportal.core.domain.dataset.FileType
import cz.machovec.lekovyportal.core.domain.mpd.MpdDatasetType
import cz.machovec.lekovyportal.core.util.ZipFileUtils
import mu.KotlinLogging

object MpdCsvExtractor {

    private val log = KotlinLogging.logger {}

    private val NESTED_ZIP_MONTH_REGEX =
        Regex(""".*(\d{4})(\d{2})\.zip""", RegexOption.IGNORE_CASE)

    /**
     * Extracts month (MM) from nested ZIP file name like `MPD_202304.zip`.
     */
    fun extractMonthFromZipName(name: String): Int? =
        NESTED_ZIP_MONTH_REGEX.matchEntire(name)
            ?.groupValues
            ?.get(2)
            ?.toIntOrNull()

    /**
     * Extracts CSV files from MPD ZIP archive and maps them by [MpdDatasetType].
     * Unknown files are ignored.
     */
    fun extractMpdCsvFiles(zipBytes: ByteArray): Map<MpdDatasetType, ByteArray> =
        ZipFileUtils.extractFilesByType(zipBytes, FileType.CSV)
            .mapNotNull { (fileName, content) ->
                MpdDatasetType.fromFileName(fileName)?.let { it to content }
                    ?: run {
                        log.debug { "Unknown MPD CSV file: $fileName" }
                        null
                    }
            }
            .toMap()
}
