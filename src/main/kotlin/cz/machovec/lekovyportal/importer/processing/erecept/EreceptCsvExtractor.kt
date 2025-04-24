package cz.machovec.lekovyportal.importer.processing.erecept

import cz.machovec.lekovyportal.utils.ZipFileUtils
import org.springframework.stereotype.Component

@Component
class EreceptCsvExtractor {
    fun extractSingleCsvFile(zipBytes: ByteArray): ByteArray {
        val files = ZipFileUtils.extractCsvFiles(zipBytes)

        if (files.isEmpty()) {
            throw IllegalStateException("ZIP file does not contain any CSV file.")
        }

        if (files.size > 1) {
            throw IllegalStateException("ZIP file contains multiple CSV files. Only one expected.")
        }

        return files.values.first()
    }
}
