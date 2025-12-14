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
     * Extracts CSV files from a ZIP archive and maps them by [MpdDatasetType].
     *
     * Ignores files that do not match any known dataset type.
     */
    fun extractMpdCsvFiles(zipBytes: ByteArray): Map<MpdDatasetType, ByteArray> {
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
