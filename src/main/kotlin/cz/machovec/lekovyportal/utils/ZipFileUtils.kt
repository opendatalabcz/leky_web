package cz.machovec.lekovyportal.utils

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ZipFileUtils {

    /**
     * Filters and returns a map of file names to contents for all CSV files in the zip.
     */
    fun extractCsvFiles(zipBytes: ByteArray): Map<String, ByteArray> {
        val result = mutableMapOf<String, ByteArray>()
        iterateFiles(ZipInputStream(zipBytes.inputStream())) { entry, content ->
            if (entry.name.endsWith(".csv", ignoreCase = true)) {
                val fileName = entry.name.substringAfterLast('/')
                result[fileName] = content
            }
        }
        return result
    }

    /**
     * Extracts nested ZIP files from a top-level ZIP archive.
     * Each returned entry is (nested zip name) -> (content of nested zip as ByteArray).
     */
    fun extractNestedZips(zipBytes: ByteArray): Map<String, ByteArray> {
        val result = mutableMapOf<String, ByteArray>()
        iterateFiles(ZipInputStream(zipBytes.inputStream())) { entry, content ->
            if (entry.name.endsWith(".zip", ignoreCase = true)) {
                result[entry.name] = content
            }
        }
        return result
    }

    /**
     * Extracts a specific file from a ZIP archive by exact file name match.
     * Returns the file content as a ByteArray, or null if the file is not found.
     */
    fun extractFileFromZip(zipBytes: ByteArray, fileName: String): ByteArray? {
        ZipInputStream(zipBytes.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name == fileName) {
                    return zis.readBytes()
                }
                entry = zis.nextEntry
            }
        }
        return null
    }

    /**
     * Iterates over all files in the given [ZipInputStream], invoking [onFile] for each regular file.
     * Automatically closes the stream when done.
     */
    private fun iterateFiles(zipInputStream: ZipInputStream, onFile: (entry: ZipEntry, content: ByteArray) -> Unit) {
        zipInputStream.use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val content = zis.readBytes()
                    onFile(entry, content)
                }
                entry = zis.nextEntry
            }
        }
    }
}