package cz.machovec.lekovyportal.processor.evaluator

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.core.repository.ProcessedDatasetRepository
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.OffsetDateTime
import java.time.YearMonth

@Component
class DatasetProcessingEvaluator(
    private val processedDatasetRepository: ProcessedDatasetRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private val FIRST_MPD_PERIOD: YearMonth = YearMonth.of(2021, 1)
    }

    /**
     * Determines whether a monthly dataset can be processed.
     */
    fun canProcessMonth(datasetType: DatasetType, year: Int, month: Int): Boolean {
        val currentPeriod = YearMonth.of(year, month)

        // 1 – Already processed -> false
        if (processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(datasetType, year, month)) {
            logger.debug { "Dataset $datasetType for $year-$month already processed – skipping." }
            return false
        }

        return when (datasetType) {
            DatasetType.MEDICINAL_PRODUCT_DATABASE -> {
                // 2 – First historical dataset -> always process
                if (currentPeriod == FIRST_MPD_PERIOD) {
                    logger.debug { "First MPD period $currentPeriod – processing allowed." }
                    return true
                }

                // 3 – Other MPD months -> previous MPD month must exist
                val previousPeriod = currentPeriod.minusMonths(1)
                val previousExists = processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(
                    DatasetType.MEDICINAL_PRODUCT_DATABASE,
                    previousPeriod.year,
                    previousPeriod.monthValue
                )
                if (!previousExists) {
                    logger.debug { "Previous MPD period $previousPeriod missing – cannot process $currentPeriod." }
                }
                previousExists
            }

            // 2 – For other dataset types -> MPD data for this month must exist
            DatasetType.ERECEPT_PRESCRIPTIONS,
            DatasetType.ERECEPT_DISPENSES,
            DatasetType.DISTRIBUTIONS_FROM_MAHS,
            DatasetType.DISTRIBUTIONS_FROM_DISTRIBUTORS,
            DatasetType.DISTRIBUTIONS_EXPORT_FROM_DISTRIBUTORS,
            DatasetType.DISTRIBUTIONS_FROM_PHARMACIES -> {
                val mpdExists = processedDatasetRepository.existsByDatasetTypeAndYearAndMonth(
                    DatasetType.MEDICINAL_PRODUCT_DATABASE,
                    year,
                    month
                )
                if (!mpdExists) {
                    logger.debug { "MPD data missing for $year-$month – cannot process $datasetType." }
                }
                mpdExists
            }
        }
    }

    /**
     * Determines whether a yearly dataset can be processed.
     */
    fun canProcessYear(datasetType: DatasetType, year: Int): Boolean {
        require(datasetType in setOf(
            DatasetType.ERECEPT_PRESCRIPTIONS,
            DatasetType.ERECEPT_DISPENSES
        )) {
            "Year-level processing is only supported for eRecept datasets."
        }

        // 1 – Any month already processed -> skip year
        if (processedDatasetRepository.findAllByDatasetTypeAndYear(datasetType, year).isNotEmpty()) {
            logger.debug { "Year $year for $datasetType already partially processed – skipping." }
            return false
        }

        // 2 – Year before first MPD period -> process if atleast one MPD dataset already processed
        if (year < FIRST_MPD_PERIOD.year) {
            val mpdAlreadyImported = processedDatasetRepository.existsByDatasetType(DatasetType.MEDICINAL_PRODUCT_DATABASE)

            if (!mpdAlreadyImported) {
                logger.debug { "No MPD datasets imported yet – cannot process yearly $datasetType ($year)." }
                return false
            }

            logger.debug { "Year $year before first MPD period & MPD present – processing allowed." }
            return true
        }

        val now = OffsetDateTime.now(clock)

        // 3 – Future year -> skip
        if (year > now.year) {
            logger.debug { "Future year $year detected – skipping processing for $datasetType." }
            return false
        }

        // 4 – Verify MPD datasets exist for all expected months
        val expectedMonths = if (year == now.year) 1..now.monthValue else 1..12
        val processedMonths = processedDatasetRepository
            .findAllByDatasetTypeAndYear(DatasetType.MEDICINAL_PRODUCT_DATABASE, year)
            .map { it.month }
            .toSet()

        val allMonthsExist = expectedMonths.all { it in processedMonths }
        if (!allMonthsExist) {
            logger.debug { "Not all MPD datasets available for $year – cannot process $datasetType." }
        }
        return allMonthsExist
    }
}
