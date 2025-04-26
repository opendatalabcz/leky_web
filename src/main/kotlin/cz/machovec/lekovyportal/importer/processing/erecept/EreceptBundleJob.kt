package cz.machovec.lekovyportal.importer.processing.erecept

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.FileType
import cz.machovec.lekovyportal.domain.entity.ProcessedDataset
import cz.machovec.lekovyportal.domain.repository.EreceptDispenseRepository
import cz.machovec.lekovyportal.domain.repository.EreceptPrescriptionRepository
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.importer.columns.erecept.EreceptCsvColumn
import cz.machovec.lekovyportal.importer.common.CsvImporter
import cz.machovec.lekovyportal.importer.common.RemoteFileDownloader
import cz.machovec.lekovyportal.importer.mapper.DataImportResult
import cz.machovec.lekovyportal.importer.mapper.erecept.EreceptRawData
import cz.machovec.lekovyportal.importer.mapper.erecept.EreceptRawDataRowMapper
import cz.machovec.lekovyportal.importer.mapper.erecept.toDispenseEntity
import cz.machovec.lekovyportal.importer.mapper.erecept.toPrescriptionEntity
import cz.machovec.lekovyportal.importer.processing.DatasetProcessingEvaluator
import cz.machovec.lekovyportal.messaging.DatasetToProcessMessage
import cz.machovec.lekovyportal.processor.DatasetProcessor
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider
import cz.machovec.lekovyportal.utils.ZipFileUtils
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
    private val referenceDataProvider: MpdReferenceDataProvider,
    private val datasetProcessingEvaluator: DatasetProcessingEvaluator,
    private val importer: CsvImporter
) : DatasetProcessor {

    private val logger = KotlinLogging.logger {}

    @Transactional
    override fun processFile(msg: DatasetToProcessMessage) {
        // Step 1 – Validate that the file type is ZIP before proceeding
        if (msg.fileType != FileType.ZIP) {
            logger.warn { "Skipping non-ZIP file type: ${msg.fileType} for dataset ${msg.datasetType}" }
            return
        }

        // Step 2 – Download the ZIP file from the remote source
        val zipBytes = downloader.downloadFile(URI(msg.fileUrl))
            ?: return logger.error { "Failed to download ZIP for ${msg.fileUrl}" }

        // Step 3 – Extract CSV file (1 file contains all data even for yearly dataset)
        val csvBytes = ZipFileUtils.extractSingleFileByType(zipBytes, FileType.CSV)

        // Step 4 - Process csv file
        if (msg.month != null) {
            if (!datasetProcessingEvaluator.canProcessMonth(msg.datasetType, msg.year, msg.month)) return
            processMonth(msg.datasetType, msg.year, msg.month, csvBytes)
        } else {
            if (!datasetProcessingEvaluator.canProcessYear(msg.datasetType, msg.year)) return
            processYear(msg.datasetType, msg.year, csvBytes)
        }
    }

    private fun processMonth(datasetType: DatasetType, year: Int, month: Int, csvBytes: ByteArray) {
        val importResult = importer.import(
            csvBytes,
            EreceptCsvColumn.entries.map { it.toSpec() },
            EreceptRawDataRowMapper()
        )
        logImportSummary(datasetType, importResult)

        val finalDataRows = aggregatePrague(importResult.successes, ::aggregationKey)

        persist(finalDataRows, datasetType, year, month)
    }

    private fun processYear(datasetType: DatasetType, year: Int, csvBytes: ByteArray) {
        val importResult = importer.import(
            csvBytes,
            EreceptCsvColumn.entries.map { it.toSpec() },
            EreceptRawDataRowMapper()
        )
        logImportSummary(datasetType, importResult)

        val groupedByMonth = importResult.successes.groupBy { it.month }

        for ((month, rowsForMonth) in groupedByMonth) {
            val finalDataRows = aggregatePrague(rowsForMonth, ::aggregationKey)
            persist(finalDataRows, datasetType, year, month)
        }
    }

    private fun aggregatePrague(
        rows: List<EreceptRawData>,
        keyExtractor: (EreceptRawData) -> String
    ): List<EreceptRawData> {
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

    private fun persist(records: List<EreceptRawData>, datasetType: DatasetType, year: Int, month: Int) {
        if (records.isEmpty()) {
            logger.info { "No records to persist for $datasetType in $year-$month" }
            return
        }

        when (datasetType) {
            DatasetType.ERECEPT_DISPENSES -> {
                val entities = records.mapNotNull { it.toDispenseEntity(referenceDataProvider) }
                dispenseRepository.batchInsert(entities)
            }
            DatasetType.ERECEPT_PRESCRIPTIONS -> {
                val entities = records.mapNotNull { it.toPrescriptionEntity(referenceDataProvider) }
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
}
