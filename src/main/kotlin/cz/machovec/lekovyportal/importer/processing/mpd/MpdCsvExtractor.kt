package cz.machovec.lekovyportal.importer.processing.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.utils.ZipFileUtils
import cz.machovec.lekovyportal.messaging.DatasetToProcessMessage
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class MpdCsvExtractor {

    // TODO: review
    private val log = KotlinLogging.logger {}

    fun extractMonthlyCsvFilesFromZip(
        zipBytes: ByteArray,
        msg: DatasetToProcessMessage
    ): Map<Int, Map<MpdDatasetType, ByteArray>> {
        return if (msg.month == null) {
            extractMonthlyZipsFromAnnualZip(zipBytes)
        } else {
            mapOf(msg.month to extractCsvFilesFromMonthlyZip(zipBytes))
        }
    }

    private fun extractMonthlyZipsFromAnnualZip(annualZip: ByteArray): Map<Int, Map<MpdDatasetType, ByteArray>> {
        val result = mutableMapOf<Int, Map<MpdDatasetType, ByteArray>>()

        ZipFileUtils.extractNestedZips(annualZip).forEach { (zipName, zipBytes) ->
            val month = parseMonthFromNestedZipFilename(zipName)
            if (month != null) {
                result[month] = extractCsvFilesFromMonthlyZip(zipBytes)
            } else {
                log.warn { "Cannot determine month from nested ZIP name: $zipName" }
            }
        }

        return result.toSortedMap()
    }

    private fun extractCsvFilesFromMonthlyZip(zipBytes: ByteArray): Map<MpdDatasetType, ByteArray> {
        val extractedCsvs = ZipFileUtils.extractCsvFiles(zipBytes)

        return extractedCsvs.mapNotNull { (fileName, content) ->
            val key = MpdDatasetType.fromFileName(fileName)
            if (key == null) {
                log.debug { "Unknown CSV file: $fileName" }
                null
            } else {
                key to content
            }
        }.toMap()
    }


    private fun parseMonthFromNestedZipFilename(name: String): Int? {
        val regex = Regex(".*(\\d{4})(\\d{2})\\.zip", RegexOption.IGNORE_CASE)
        return regex.matchEntire(name)?.groupValues?.get(2)?.toIntOrNull()
    }
}
