package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.domain.entity.EreceptDispense
import cz.machovec.lekovyportal.domain.entity.FileType
import cz.machovec.lekovyportal.domain.repository.EreceptDispenseRepository
import cz.machovec.lekovyportal.messaging.NewFileMessage
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.net.URL
import java.util.zip.ZipInputStream

private val logger = KotlinLogging.logger {}

@Service
class EreceptDispenseFileProcessor(
    private val ereceptDispenseRepository: EreceptDispenseRepository
) : DatasetFileProcessor {

    override fun processFile(msg: NewFileMessage) {
        val fileBytes = URL(msg.fileUrl).readBytes()
        val records = if (msg.fileType == FileType.ZIP) {
            parseZip(fileBytes)
        } else {
            parseCsv(fileBytes)
        }

        logger.info("Processing file: ${msg.fileUrl}, records count: ${records.size}")

        ereceptDispenseRepository.batchInsert(records, batchSize = 50)
    }

    private fun parseZip(zipBytes: ByteArray): List<EreceptDispense> {
        val result = mutableListOf<EreceptDispense>()
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".csv", ignoreCase = true)) {
                    result += parseCsv(zis.readBytes())
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return result
    }

    private fun parseCsv(csvBytes: ByteArray): List<EreceptDispense> {
        val text = csvBytes.decodeToString()
        val lines = text.split("\r\n", "\n").filter { it.isNotBlank() }

        return lines.drop(1).mapNotNull { line ->

            val cols = line.split(",").map { it.trim('"') }

            if (cols.size < 10) return@mapNotNull null

            val districtCode = cols[0]
            val year = cols[2].toIntOrNull() ?: return@mapNotNull null
            val month = cols[3].toIntOrNull() ?: return@mapNotNull null
            val suklCode = cols[4]
            val quantity = cols[9].toIntOrNull() ?: return@mapNotNull null

            EreceptDispense(
                districtCode = districtCode,
                year = year,
                month = month,
                suklCode = suklCode,
                quantity = quantity
            )
        }
    }

}
