package cz.machovec.lekovyportal.processor.processing.erecept

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.core.domain.dataset.FileType
import cz.machovec.lekovyportal.core.domain.dataset.ProcessedDataset
import cz.machovec.lekovyportal.core.domain.erecept.District
import cz.machovec.lekovyportal.core.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.core.repository.erecept.EreceptDispenseRepository
import cz.machovec.lekovyportal.core.repository.erecept.EreceptPrescriptionRepository
import cz.machovec.lekovyportal.messaging.dto.DatasetToProcessMessage
import cz.machovec.lekovyportal.processor.evaluator.DatasetProcessingEvaluator
import cz.machovec.lekovyportal.processor.mapper.MutableImportStats
import cz.machovec.lekovyportal.processor.mapper.erecept.EreceptCsvColumn
import cz.machovec.lekovyportal.processor.mapper.erecept.EreceptRawData
import cz.machovec.lekovyportal.processor.mapper.erecept.EreceptRawDataRowMapper
import cz.machovec.lekovyportal.processor.mapper.erecept.toDispenseEntity
import cz.machovec.lekovyportal.processor.mapper.erecept.toPrescriptionEntity
import cz.machovec.lekovyportal.processor.mapper.toSpec
import cz.machovec.lekovyportal.processor.processing.CsvImporter
import cz.machovec.lekovyportal.processor.processing.DatasetProcessor
import cz.machovec.lekovyportal.processor.processing.distribution.DistrictReferenceDataProvider
import cz.machovec.lekovyportal.processor.processing.mpd.MpdReferenceDataProvider
import cz.machovec.lekovyportal.processor.util.RemoteFileDownloader
import mu.KotlinLogging
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.net.URI
import java.util.zip.ZipInputStream

@Service
class EreceptProcessor(
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
        private val CHARSET = Charsets.UTF_8
        private const val CSV_DATA_SEPARATOR: Char = ','
        private const val BATCH_SIZE: Int = 5_000
        private const val PRAGUE_CODE: String = "3100"
    }

    @Transactional
    override fun processFile(msg: DatasetToProcessMessage) {

        /* -------------------------------------------------
         * STEP 0 – Basic message validation
         * ------------------------------------------------- */

        require(msg.fileType == FileType.ZIP) {
            "eRecept datasets must be provided as ZIP (fileType=${msg.fileType})"
        }

        require(msg.datasetType in setOf(
            DatasetType.ERECEPT_PRESCRIPTIONS,
            DatasetType.ERECEPT_DISPENSES
        )) {
            "Unsupported datasetType for eRecept processor: ${msg.datasetType}"
        }

        /* -------------------------------------------------
         * STEP 1 – Evaluator (BEFORE download)
         * ------------------------------------------------- */

        val canProcess = msg.month?.let {
            datasetProcessingEvaluator.canProcessMonth(msg.datasetType, msg.year, it)
        } ?: datasetProcessingEvaluator.canProcessYear(msg.datasetType, msg.year)

        if (!canProcess) {
            logger.info {
                "Dataset ${msg.datasetType} ${msg.year}-${msg.month ?: "*"} cannot be processed (evaluator)."
            }
            return
        }

        /* -------------------------------------------------
         * STEP 2 – Download source file
         * ------------------------------------------------- */

        val zipBytes = downloader.downloadFile(URI(msg.fileUrl))
            ?: return logger.error { "Failed to download ZIP for ${msg.fileUrl}" }

        val districtMap = districtReferenceDataProvider.getDistrictMap()

        /* -------------------------------------------------
         * STEP 3 – Open ZIP & locate CSV
         * ------------------------------------------------- */

        ByteArrayInputStream(zipBytes).use { byteStream ->
            ZipInputStream(byteStream).use { zipStream ->

                val csvEntry = generateSequence { zipStream.nextEntry }
                    .firstOrNull { it.name.endsWith(".csv", ignoreCase = true) }
                    ?: run {
                        logger.error { "No CSV file found in ZIP for ${msg.fileUrl}" }
                        return
                    }

                /* -------------------------------------------------
                 * STEP 4 – Stream CSV, import & persist
                 * ------------------------------------------------- */

                val reader = BufferedReader(
                    InputStreamReader(
                        BOMInputStream.builder()
                            .setInputStream(zipStream)
                            .setInclude(false)
                            .get(),
                        CHARSET
                    )
                )

                val importStats = MutableImportStats()

                val dataSequence = importer.importStream(
                    reader,
                    EreceptCsvColumn.entries.map { it.toSpec() },
                    EreceptRawDataRowMapper(),
                    CSV_DATA_SEPARATOR,
                    importStats
                )

                val processedMonths = processStream(
                    sequence = dataSequence,
                    datasetType = msg.datasetType,
                    year = msg.year,
                    districtMap = districtMap
                )

                /* -------------------------------------------------
                 * STEP 5 – Logging
                 * ------------------------------------------------- */

                logEreceptImport(msg.datasetType, msg.year, processedMonths, importStats)
            }
        }
    }

    /* =================================================
     * STEP 4 – Stream processing (month-level logic)
     * ================================================= */

    private fun processStream(
        sequence: Sequence<EreceptRawData>,
        datasetType: DatasetType,
        year: Int,
        districtMap: Map<String, District>
    ): Set<Int> {

        val pragueAggregate = mutableMapOf<String, EreceptRawData>()
        val nonPragueBuffer = mutableListOf<EreceptRawData>()
        val processedMonths = mutableSetOf<Int>()

        sequence.forEach { row ->
            if (row.districtCode == PRAGUE_CODE) {
                val key = aggregationKey(row)
                pragueAggregate.merge(key, row) { oldVal, newVal ->
                    oldVal.copy(quantity = oldVal.quantity + newVal.quantity)
                }
            } else {
                nonPragueBuffer.add(row)
                if (nonPragueBuffer.size >= BATCH_SIZE) {
                    flushBuffer(nonPragueBuffer, datasetType, districtMap, processedMonths)
                    nonPragueBuffer.clear()
                }
            }
        }

        if (nonPragueBuffer.isNotEmpty()) {
            flushBuffer(nonPragueBuffer, datasetType, districtMap, processedMonths)
        }

        if (pragueAggregate.isNotEmpty()) {
            pragueAggregate.values.chunked(BATCH_SIZE).forEach { batch ->
                flushBuffer(batch, datasetType, districtMap, processedMonths)
            }
        }

        /* -------------------------------------------------
         * STEP 5 – Mark processed months
         * ------------------------------------------------- */

        processedMonths.forEach { month ->
            processedDatasetRepository.save(
                ProcessedDataset(
                    datasetType = datasetType,
                    year = year,
                    month = month
                )
            )
        }

        return processedMonths
    }

    private fun flushBuffer(
        buffer: Collection<EreceptRawData>,
        datasetType: DatasetType,
        districtMap: Map<String, District>,
        processedMonths: MutableSet<Int>
    ) {
        if (buffer.isEmpty()) return

        buffer.groupBy { it.month }.forEach { (month, rows) ->
            val persisted = persist(rows, datasetType, districtMap)
            if (persisted) {
                processedMonths.add(month)
            }
        }
    }

    private fun persist(
        records: List<EreceptRawData>,
        datasetType: DatasetType,
        districtMap: Map<String, District>
    ): Boolean {
        if (records.isEmpty()) return false

        return when (datasetType) {
            DatasetType.ERECEPT_DISPENSES -> {
                val entities = records.mapNotNull {
                    it.toDispenseEntity(mpdReferenceDataProvider, districtMap)
                }
                if (entities.isNotEmpty()) {
                    dispenseRepository.batchInsert(entities)
                    true
                } else false
            }

            DatasetType.ERECEPT_PRESCRIPTIONS -> {
                val entities = records.mapNotNull {
                    it.toPrescriptionEntity(mpdReferenceDataProvider, districtMap)
                }
                if (entities.isNotEmpty()) {
                    prescriptionRepository.batchInsert(entities)
                    true
                } else false
            }

            else -> error("Unsupported datasetType $datasetType")
        }
    }

    private fun aggregationKey(row: EreceptRawData): String =
        "${row.districtCode}-${row.year}-${row.month}-${row.suklCode}"

    /* =================================================
     * Logging
     * ================================================= */

    private fun logEreceptImport(
        datasetType: DatasetType,
        year: Int,
        processedMonths: Set<Int>,
        stats: MutableImportStats
    ) {
        val scope = formatScope(year, processedMonths)

        if (stats.totalFailures == 0L) {
            logger.info {
                "IMPORT OK | $datasetType | $scope | " +
                        "success=${stats.successCount}/${stats.totalRows} | " +
                        "rate=${stats.successRatePercent()}%"
            }
            return
        }

        logger.warn {
            "IMPORT WARN | $datasetType | $scope | " +
                    "success=${stats.successCount}/${stats.totalRows} | " +
                    "rate=${stats.successRatePercent()}%"
        }

        stats.failuresByReason()
            .takeIf { it.isNotEmpty() }
            ?.let {
                logger.warn {
                    "IMPORT WARN | failures by reason: " +
                            it.entries.joinToString { e -> "${e.key}=${e.value}" }
                }
            }

        stats.failuresByReasonAndColumn()
            .takeIf { it.isNotEmpty() }
            ?.let {
                logger.warn {
                    "IMPORT WARN | failures by column: " +
                            it.entries.joinToString { e ->
                                val (_, column) = e.key
                                "${column ?: "?"}=${e.value}"
                            }
                }
            }
    }

    private fun formatScope(year: Int, months: Set<Int>): String =
        when {
            months.isEmpty() -> "$year (no data)"
            months.size == 1 -> "$year-${months.first()}"
            months.size == 12 -> "$year (all months)"
            else -> "$year months=${months.sorted().joinToString(",")}"
        }
}
