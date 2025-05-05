package cz.machovec.lekovyportal.processor.processing.erecept

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.core.domain.erecept.District
import cz.machovec.lekovyportal.core.domain.dataset.FileType
import cz.machovec.lekovyportal.core.domain.dataset.ProcessedDataset
import cz.machovec.lekovyportal.core.repository.erecept.EreceptDispenseRepository
import cz.machovec.lekovyportal.core.repository.erecept.EreceptPrescriptionRepository
import cz.machovec.lekovyportal.core.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.processor.processing.CsvImporter
import cz.machovec.lekovyportal.processor.util.RemoteFileDownloader
import cz.machovec.lekovyportal.processor.mapper.DataImportResult
import cz.machovec.lekovyportal.processor.mapper.erecept.EreceptCsvColumn
import cz.machovec.lekovyportal.processor.mapper.erecept.EreceptRawData
import cz.machovec.lekovyportal.processor.mapper.erecept.EreceptRawDataRowMapper
import cz.machovec.lekovyportal.processor.mapper.erecept.toDispenseEntity
import cz.machovec.lekovyportal.processor.mapper.erecept.toPrescriptionEntity
import cz.machovec.lekovyportal.processor.mapper.toSpec
import cz.machovec.lekovyportal.processor.evaluator.DatasetProcessingEvaluator
import cz.machovec.lekovyportal.processor.processing.distribution.DistrictReferenceDataProvider
import cz.machovec.lekovyportal.processor.processing.mpd.MpdReferenceDataProvider
import cz.machovec.lekovyportal.messaging.dto.DatasetToProcessMessage
import cz.machovec.lekovyportal.processor.processing.DatasetProcessor
import cz.machovec.lekovyportal.core.util.ZipFileUtils
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI

@Service
class EreceptBundleJob(
    private val processedDatasetRepository: ProcessedDatasetRepository,
    private val dispenseRepository: EreceptDispenseRepository,
    private val prescriptionRepository: EreceptPrescriptionRepository,
    private val downloader: RemoteFileDownloader,
    private val mpdReferenceDataProvider: MpdReferenceDataProvider,
    private val districtReferenceDataProvider: DistrictReferenceDataProvider,
    private val datasetProcessingEvaluator: DatasetProcessingEvaluator,
    private val importer: CsvImporter
) : DatasetProcessor {

    private val logger = KotlinLogging.logger {}

    companion object {
        private const val ERECEPT_CSV_DATA_SEPARATOR: Char = ','
    }

    @Transactional
    override fun processFile(msg: DatasetToProcessMessage) {
        if (msg.fileType != FileType.ZIP) {
            logger.warn { "Skipping non-ZIP file type: ${msg.fileType} for dataset ${msg.datasetType}" }
            return
        }

        // Step 2 – Download the ZIP file from the remote source
        val zipBytes = downloader.downloadFile(URI(msg.fileUrl))
            ?: return logger.error { "Failed to download ZIP for ${msg.fileUrl}" }

        // Step 3 – Extract CSV file (1 file contains all data even for yearly dataset)
        val rawCsvBytes = ZipFileUtils.extractSingleFileByType(zipBytes, FileType.CSV)
        val csvBytes = rawCsvBytes?.let { removeBomIfPresent(it) } ?: return

        val districtMap = districtReferenceDataProvider.getDistrictMap()

        // Step 4 - Process csv file
        if (msg.month != null) {
            if (!datasetProcessingEvaluator.canProcessMonth(msg.datasetType, msg.year, msg.month)) return
            processMonth(msg.datasetType, msg.year, msg.month, csvBytes, districtMap)
        } else {
            if (!datasetProcessingEvaluator.canProcessYear(msg.datasetType, msg.year)) return
            processYear(msg.datasetType, msg.year, csvBytes, districtMap)
        }
    }

    private fun processMonth(datasetType: DatasetType, year: Int, month: Int, csvBytes: ByteArray, districtMap: Map<String, District>) {
        val importResult = importer.import(
            csvBytes,
            EreceptCsvColumn.entries.map { it.toSpec() },
            EreceptRawDataRowMapper(),
            ERECEPT_CSV_DATA_SEPARATOR
        )
        logImportSummary(datasetType, importResult)

        val finalDataRows = aggregatePrague(importResult.successes, ::aggregationKey)

        persist(finalDataRows, datasetType, year, month, districtMap)
    }

    private fun processYear(datasetType: DatasetType, year: Int, csvBytes: ByteArray, districtMap: Map<String, District>) {
        val importResult = importer.import(
            csvBytes,
            EreceptCsvColumn.entries.map { it.toSpec() },
            EreceptRawDataRowMapper(),
            ERECEPT_CSV_DATA_SEPARATOR
        )
        logImportSummary(datasetType, importResult)

        val groupedByMonth = importResult.successes.groupBy { it.month }

        for ((month, rowsForMonth) in groupedByMonth) {
            val finalDataRows = aggregatePrague(rowsForMonth, ::aggregationKey)
            persist(finalDataRows, datasetType, year, month, districtMap)
        }
    }

    private fun aggregatePrague(rows: List<EreceptRawData>, keyExtractor: (EreceptRawData) -> String): List<EreceptRawData> {
        val PRAGUE_CODE = "3100"
        val pragueMap = mutableMapOf<String, EreceptRawData>()
        val others = mutableListOf<EreceptRawData>()

        for (row in rows) {
            if (row.districtCode == PRAGUE_CODE) {
                val key = keyExtractor(row)
                val existing = pragueMap[key]
                if (existing != null) {
                    pragueMap[key] = existing.copy(quantity = existing.quantity + row.quantity)
                } else {
                    pragueMap[key] = row
                }
            } else {
                others += row
            }
        }
        return others + pragueMap.values
    }

    private fun persist(records: List<EreceptRawData>, datasetType: DatasetType, year: Int, month: Int, districtMap: Map<String, District>) {
        if (records.isEmpty()) {
            logger.info { "No records to persist for $datasetType in $year-$month" }
            return
        }

        when (datasetType) {
            DatasetType.ERECEPT_DISPENSES -> {
                val entities = records.mapNotNull { it.toDispenseEntity(mpdReferenceDataProvider, districtMap) }
                dispenseRepository.batchInsert(entities)
            }
            DatasetType.ERECEPT_PRESCRIPTIONS -> {
                val entities = records.mapNotNull { it.toPrescriptionEntity(mpdReferenceDataProvider, districtMap) }
                prescriptionRepository.batchInsert(entities)
            }
            else -> logger.warn { "Unsupported datasetType: $datasetType" }
        }

        processedDatasetRepository.save(
            ProcessedDataset(
                datasetType = datasetType,
                year = year,
                month = month
            )
        )
    }

    private fun aggregationKey(row: EreceptRawData): String =
        "${row.districtCode}-${row.year}-${row.month}-${row.suklCode}"

    private fun <T> logImportSummary(datasetType: DatasetType, result: DataImportResult<T>) {
        if (result.failures.isEmpty()) {
            logger.info { "Import of $datasetType completed successfully (${result.successes.size}/${result.totalRows} rows)." }
            return
        }

        logger.warn {
            val reasonSummary = result.failuresByReason()
                .entries
                .joinToString { "${it.key}: ${it.value}" }

            val detailedSummary = result.failuresByReasonAndColumn()
                .entries
                .joinToString { (reasonAndColumn, count) ->
                    val (reason, column) = reasonAndColumn
                    "$reason in column '$column': $count"
                }

            """
        Import of $datasetType completed with errors:
          - Success: ${result.successes.size}/${result.totalRows}
          - Failures by reason: $reasonSummary
          - Failures by reason and column:
            $detailedSummary
        """.trimIndent()
        }
    }

    private fun removeBomIfPresent(bytes: ByteArray): ByteArray {
        if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() &&
            bytes[1] == 0xBB.toByte() &&
            bytes[2] == 0xBF.toByte()) {
            return bytes.drop(3).toByteArray() // remove BOM
        }
        return bytes
    }
}
