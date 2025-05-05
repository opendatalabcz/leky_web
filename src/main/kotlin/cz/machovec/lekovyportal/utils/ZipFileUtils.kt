package cz.machovec.lekovyportal.utils

import cz.machovec.lekovyportal.domain.entity.FileType
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ZipFileUtils {

    /**
     * Extracts all files from the ZIP that satisfy all given [filters].
     * Each file is returned as a pair (fileName -> fileContent).
     * File names are stripped to just the name (without inner folder path).
     */
    private fun extractFilesMatchingAll(
        zipBytes: ByteArray,
        filters: List<(ZipEntry) -> Boolean>
    ): Map<String, ByteArray> {
        val result = mutableMapOf<String, ByteArray>()
        iterateFiles(ZipInputStream(zipBytes.inputStream())) { entry, content ->
            val matchesAll = filters.all { it(entry) }
            if (matchesAll) {
                result[entry.name.substringAfterLast('/')] = content
            }
        }
        return result
    }

    /**
     * Filters and returns a map of file names to contents for all files in the zip matching the given [fileType].
     */
    fun extractFilesByType(zipBytes: ByteArray, fileType: FileType): Map<String, ByteArray> =
        extractFilesMatchingAll(zipBytes, listOf(
            { it.name.endsWith(fileType.extension, ignoreCase = true) }
        ))

    /**
     * Returns the only file from the ZIP archive matching the given [fileType].
     * Throws IllegalStateException if there are none or more than one.
     */
    fun extractSingleFileByType(zipBytes: ByteArray, fileType: FileType): ByteArray =
        extractFilesByType(zipBytes, fileType).let { files ->
            when {
                files.isEmpty() -> throw IllegalStateException("ZIP does not contain any ${fileType.extension} files.")
                files.size > 1  -> throw IllegalStateException("ZIP contains multiple ${fileType.extension} files – expected exactly one.")
                else            -> files.values.first()
            }
        }

    /**
     * Returns the content of a file with exact [fileName] inside the ZIP archive.
     * Returns null if the file is not found.
     */
    fun extractFileByName(zipBytes: ByteArray, fileName: String): ByteArray? =
        extractFilesMatchingAll(zipBytes, listOf(
            { it.name == fileName }
        )).values.firstOrNull()

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