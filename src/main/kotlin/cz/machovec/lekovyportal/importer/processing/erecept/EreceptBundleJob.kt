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
    private val datasetProcessingEvaluator: DatasetProcessingEvaluator
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
        val csvFile = ZipFileUtils.extractSingleFileByType(zipBytes, FileType.CSV)

        // Step 4 - Process csv file
        if (msg.month != null) {
            if (!datasetProcessingEvaluator.canProcessMonth(msg.datasetType, msg.year, msg.month)) return
            processMonth(msg.datasetType, msg.year, msg.month, csvFile)
        } else {
            if (!datasetProcessingEvaluator.canProcessYear(msg.datasetType, msg.year)) return
            processYear(msg.datasetType, msg.year, csvFile)
        }
    }

    private fun processMonth(
        datasetType: DatasetType,
        year: Int,
        month: Int,
        csvBytes: ByteArray
    ) {
        // Step 1 - Map csv rows to data rows
        val rawDataRows = CsvImporter().import(
            csvBytes,
            EreceptCsvColumn.entries.map { it.toSpec() },
            EreceptRawDataRowMapper()
        )

        // Step 2 - Aggregate multiple Prague data rows
        val finalDataRows = aggregatePrague(rawDataRows, ::aggregationKey)

        // Step 3 - Map data rows to entities and persist them
        when (datasetType) {
            DatasetType.ERECEPT_DISPENSES -> {
                val entities = finalDataRows.mapNotNull { it.toDispenseEntity(referenceDataProvider) }
                persist(entities, datasetType, year, month) {
                    dispenseRepository.batchInsert(entities)
                }
            }

            DatasetType.ERECEPT_PRESCRIPTIONS -> {
                val entities = finalDataRows.mapNotNull { it.toPrescriptionEntity(referenceDataProvider) }
                persist(entities, datasetType, year, month) {
                    prescriptionRepository.batchInsert(entities)
                }
            }

            else -> logger.warn { "Unsupported datasetType: ${datasetType}" }
        }

        processedDatasetRepository.save(
            ProcessedDataset(
                datasetType = datasetType,
                year = year,
                month = month
            )
        )
    }

    private fun processYear(
        datasetType: DatasetType,
        year: Int,
        csvBytes: ByteArray
    ) {
        // Step 1 - Map csv rows to data rows
        val rawDataRows = CsvImporter().import(
            csvBytes,
            EreceptCsvColumn.entries.map { it.toSpec() },
            EreceptRawDataRowMapper()
        )

        // Step 2 - Group data rows by months
        val groupedByMonth = rawDataRows.groupBy { it.month }

        for ((month, rowsForMonth) in groupedByMonth) {
            val aggregated = aggregatePrague(rowsForMonth, ::aggregationKey)

            when (datasetType) {
                DatasetType.ERECEPT_DISPENSES -> {
                    val entities = aggregated.mapNotNull { it.toDispenseEntity(referenceDataProvider) }
                    persist(entities, datasetType, year, month) {
                        dispenseRepository.batchInsert(it)
                    }
                }

                DatasetType.ERECEPT_PRESCRIPTIONS -> {
                    val entities = aggregated.mapNotNull { it.toPrescriptionEntity(referenceDataProvider) }
                    persist(entities, datasetType, year, month) {
                        prescriptionRepository.batchInsert(it)
                    }
                }

                else -> logger.warn { "Unsupported datasetType: $datasetType" }
            }
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

    private fun <T> persist(
        records: List<T>,
        datasetType: DatasetType,
        year: Int,
        month: Int,
        batchInsert: (List<T>) -> Unit
    ) {
        if (records.isEmpty()) {
            logger.info { "No records to save for $datasetType in $year-$month" }
            return
        }

        logger.info { "Saving ${records.size} records for $datasetType in $year-$month" }
        batchInsert(records)

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
}
