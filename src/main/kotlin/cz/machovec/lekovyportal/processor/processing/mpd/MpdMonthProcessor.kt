package cz.machovec.lekovyportal.processor.processing.mpd

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.core.domain.dataset.ProcessedDataset
import cz.machovec.lekovyportal.core.domain.mpd.MpdDatasetType
import cz.machovec.lekovyportal.core.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.core.util.mpd.MpdValidityReader
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth

@Service
class MpdMonthProcessor(
    private val tablesProcessor: MpdTablesProcessor,
    private val processedDatasetRepository: ProcessedDatasetRepository,
    private val validityReader: MpdValidityReader,
    private val referenceDataProvider: MpdReferenceDataProvider
) {

    private val logger = KotlinLogging.logger {}

    companion object {
        private val DATASET_TYPE = DatasetType.MEDICINAL_PRODUCT_DATABASE
        private val VALIDITY_REQUIRED_SINCE = YearMonth.of(2023, 4)
    }

    @Transactional
    fun processMonth(
        period: YearMonth,
        csvMap: Map<MpdDatasetType, ByteArray>
    ) {

        /* -------------------------------------------------
         * STEP 4 – Import & persist MPD tables
         * ------------------------------------------------- */

        val validFrom = resolveValidFrom(period, csvMap)

        tablesProcessor.processTables(
            csvMap = csvMap,
            validFrom = validFrom
        )

        /* -------------------------------------------------
         * STEP 5 – Mark dataset as processed
         * ------------------------------------------------- */

        processedDatasetRepository.save(
            ProcessedDataset(
                datasetType = DATASET_TYPE,
                year = period.year,
                month = period.monthValue
            )
        )

        referenceDataProvider.clearCache()

        logger.info {
            "Transaction finished for MPD ${period.year}-${period.monthValue}"
        }
    }

    private fun resolveValidFrom(
        period: YearMonth,
        csvMap: Map<MpdDatasetType, ByteArray>
    ): LocalDate =
        if (period < VALIDITY_REQUIRED_SINCE) {
            period.atDay(1)
        } else {
            val validityCsv = csvMap[MpdDatasetType.MPD_VALIDITY]
                ?: error("Validity CSV missing for $period")

            validityReader.getValidityFromCsv(validityCsv)?.validFrom
                ?: error("Unable to parse validity for $period")
        }
}
