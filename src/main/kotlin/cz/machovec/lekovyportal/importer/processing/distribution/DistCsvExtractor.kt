package cz.machovec.lekovyportal.importer.processing.distribution

import cz.machovec.lekovyportal.domain.entity.FileType
import cz.machovec.lekovyportal.utils.ZipFileUtils
import org.springframework.stereotype.Component

@Component
class DistCsvExtractor {
    fun extractCsvFilesByMonth(
        fileType: FileType,
        month: Int?,
        fileBytes: ByteArray
    ): Map<Int, ByteArray> {
        return if (month != null) {
            val csvBytes = if (fileType == FileType.ZIP) {
                extractSingleCsvFile(fileBytes)
            } else {
                fileBytes
            }
            mapOf(month to csvBytes)
        } else {
            extractCsvFiles(fileBytes) // Map<String, ByteArray>
                .mapNotNull { (fileName, csvBytes) ->
                    val month = extractMonthFromCsv(fileName)
                    if (month != null) month to csvBytes else null
                }.toMap()
        }
    }

    private fun extractCsvFiles(zip: ByteArray): Map<String, ByteArray> =
        ZipFileUtils.extractCsvFiles(zip)

    private fun extractSingleCsvFile(zipBytes: ByteArray): ByteArray {
        val files = ZipFileUtils.extractCsvFiles(zipBytes)

        if (files.isEmpty()) {
            throw IllegalStateException("ZIP file does not contain any CSV file.")
        }

        if (files.size > 1) {
            throw IllegalStateException("ZIP file contains multiple CSV files. Only one expected.")
        }

        return files.values.first()
    }

    private fun extractMonthFromCsv(fileName: String): Int? {
        val regex = Regex("""LEK13_\d{4}(\d{2})v\d+\.csv""")
        val match = regex.matchEntire(fileName)
        return match?.groups?.get(1)?.value?.toIntOrNull()
    }
}
