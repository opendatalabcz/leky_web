package cz.machovec.lekovyportal.api.service

import EreceptAggregateByDistrictRequest
import EreceptAggregateByDistrictResponse
import EreceptFullTimeSeriesRequest
import EreceptFullTimeSeriesResponse
import EreceptTimeSeriesByDistrictRequest
import EreceptTimeSeriesByDistrictResponse
import FullTimeSeriesEntry
import SummaryValues
import TimeSeriesMonthDistrictValues
import cz.machovec.lekovyportal.api.model.enums.CalculationMode
import cz.machovec.lekovyportal.api.model.enums.EreceptType
import cz.machovec.lekovyportal.api.model.enums.NormalisationMode
import cz.machovec.lekovyportal.api.model.enums.TimeGranularity
import cz.machovec.lekovyportal.api.model.mpd.MedicinalProductIdentificators
import cz.machovec.lekovyportal.core.domain.mpd.MpdMedicinalProduct
import cz.machovec.lekovyportal.core.repository.erecept.EReceptDistrictDataRow
import cz.machovec.lekovyportal.core.repository.erecept.EReceptMonthlyDistrictAggregate
import cz.machovec.lekovyportal.core.repository.erecept.EreceptRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdMedicinalProductRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Service
class EreceptService(
    private val ereceptRepository: EreceptRepository,
    private val medicinalProductRepository: MpdMedicinalProductRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getAggregatedByDistrict(request: EreceptAggregateByDistrictRequest): EreceptAggregateByDistrictResponse {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
        val from: YearMonth? = request.dateFrom?.let { YearMonth.parse(it, formatter) }
        val to: YearMonth? = request.dateTo?.let { YearMonth.parse(it, formatter) }

        val productsById = if (request.medicinalProductIds.isNotEmpty()) {
            medicinalProductRepository.findAllByIdIn(request.medicinalProductIds)
        } else emptyList()

        val productsByRegNumbers = if (request.registrationNumbers.isNotEmpty()) {
            medicinalProductRepository.findAllByRegistrationNumberIn(request.registrationNumbers)
        } else emptyList()

        val allProducts = (productsById + productsByRegNumbers).distinctBy { it.id }

        val (included, ignored) = allProducts.partition {
            request.calculationMode != CalculationMode.DAILY_DOSES ||
                    (it.dailyDosePackaging != null && it.dailyDosePackaging > BigDecimal.ZERO)
        }

        val startedAt = System.currentTimeMillis()
        val rows = ereceptRepository.findAggregatesAllDistricts(
            medicinalProductIds = included.mapNotNull { it.id },
            dateFrom = from,
            dateTo = to
        )
        val durationMs = System.currentTimeMillis() - startedAt
        logger.info("TIME-AGGREGATE BY DISTRICT - REPO: $durationMs ms")

        val districtValues = when (request.normalisationMode) {
            NormalisationMode.PER_1000_CAPITA -> when (request.calculationMode) {
                CalculationMode.PACKAGES -> aggregateAndNormalize(rows, request.aggregationType) { it.prescribed to it.dispensed }
                CalculationMode.DAILY_DOSES -> aggregateAndNormalizeWithDDD(rows, included, request.aggregationType)
            }
            NormalisationMode.ABSOLUTE -> when (request.calculationMode) {
                CalculationMode.PACKAGES -> aggregate(rows, request.aggregationType) { it.prescribed to it.dispensed }
                CalculationMode.DAILY_DOSES -> aggregateWithDDD(rows, included, request.aggregationType)
            }
        }

        val summary = calculateSummaryFromRows(rows)

        return EreceptAggregateByDistrictResponse(
            aggregationType = request.aggregationType,
            calculationMode = request.calculationMode,
            normalisationMode = request.normalisationMode,
            dateFrom = request.dateFrom,
            dateTo = request.dateTo,
            districtValues = districtValues,
            includedMedicineProducts = included.map { MedicinalProductIdentificators(it.id!!, it.suklCode) },
            ignoredMedicineProducts = ignored.map { MedicinalProductIdentificators(it.id!!, it.suklCode) },
            summary = summary
        )
    }

    fun getTimeSeriesByDistrict(
        request: EreceptTimeSeriesByDistrictRequest
    ): EreceptTimeSeriesByDistrictResponse {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
        val from = YearMonth.parse(request.dateFrom, formatter)
        val to = YearMonth.parse(request.dateTo, formatter)

        val productsById = if (request.medicinalProductIds.isNotEmpty()) {
            medicinalProductRepository.findAllByIdIn(request.medicinalProductIds)
        } else emptyList()

        val productsByRegNumbers = if (request.registrationNumbers.isNotEmpty()) {
            medicinalProductRepository.findAllByRegistrationNumberIn(request.registrationNumbers)
        } else emptyList()

        val allProducts = (productsById + productsByRegNumbers).distinctBy { it.id }

        val (included, ignored) = allProducts.partition {
            request.calculationMode != CalculationMode.DAILY_DOSES ||
                    (it.dailyDosePackaging != null && it.dailyDosePackaging > BigDecimal.ZERO)
        }

        val includedIds = included.mapNotNull { it.id }

        val startedAt = System.currentTimeMillis()
        val rawAggregates = ereceptRepository.findMonthlyAllDistricts(
            medicinalProductIds = includedIds,
            dateFrom = from,
            dateTo = to
        )
        val durationMs = System.currentTimeMillis() - startedAt
        logger.info("TIME-SERIES BY DISTRICT - REPO: $durationMs ms")

        val aggregatesByMonth = rawAggregates.groupBy { "${it.year}-${"%02d".format(it.month)}" }

        val series = aggregatesByMonth.entries.sortedBy { it.key }.map { (monthKey, rows) ->
            val districtValues = when (request.normalisationMode) {
                NormalisationMode.PER_1000_CAPITA -> when (request.calculationMode) {
                    CalculationMode.PACKAGES -> aggregateMonthlyAndNormalize( rows, request.aggregationType) { it.prescribed to it.dispensed }
                    CalculationMode.DAILY_DOSES -> aggregateMonthlyAndNormalizeWithDDD(rows, included, request.aggregationType)
                }
                NormalisationMode.ABSOLUTE -> when (request.calculationMode) {
                    CalculationMode.PACKAGES -> aggregateMonthly(rows, request.aggregationType) { it.prescribed to it.dispensed }
                    CalculationMode.DAILY_DOSES -> aggregateMonthlyWithDDD(rows, included, request.aggregationType)
                }
            }

            val summary = calculateSummaryFromRawRows(rows)

            TimeSeriesMonthDistrictValues(
                month = monthKey,
                values = districtValues,
                summary = summary
            )
        }

        return EreceptTimeSeriesByDistrictResponse(
            aggregationType = request.aggregationType,
            calculationMode = request.calculationMode,
            normalisationMode = request.normalisationMode,
            dateFrom = request.dateFrom,
            dateTo = request.dateTo,
            series = series,
            includedMedicineProducts = included.map { MedicinalProductIdentificators(it.id!!, it.suklCode) },
            ignoredMedicineProducts = ignored.map { MedicinalProductIdentificators(it.id!!, it.suklCode) }
        )
    }

    fun getFullTimeSeries(request: EreceptFullTimeSeriesRequest): EreceptFullTimeSeriesResponse {
        val productsById = if (request.medicinalProductIds.isNotEmpty()) {
            medicinalProductRepository.findAllByIdIn(request.medicinalProductIds)
        } else emptyList()

        val productsByRegNumbers = if (request.registrationNumbers.isNotEmpty()) {
            medicinalProductRepository.findAllByRegistrationNumberIn(request.registrationNumbers)
        } else emptyList()

        val allProducts = (productsById + productsByRegNumbers).distinctBy { it.id }

        val (included, ignored) = allProducts.partition {
            request.calculationMode != CalculationMode.DAILY_DOSES ||
                    (it.dailyDosePackaging != null && it.dailyDosePackaging > BigDecimal.ZERO)
        }

        val rawData = ereceptRepository.findFullMonthly(
            medicinalProductIds = included.mapNotNull { it.id }
        )

        val filtered = rawData.filter { request.district == null || it.districtCode == request.district }

        val grouped = when (request.timeGranularity) {
            TimeGranularity.MONTH -> filtered.groupBy { "%04d-%02d".format(it.year, it.month) }
            TimeGranularity.YEAR -> filtered.groupBy { it.year.toString() }
        }

        val series = grouped.entries.sortedBy { it.key }.map { (period, rows) ->
            val prescribed = rows.sumOf { it.prescribed }
            val dispensed = rows.sumOf { it.dispensed }
            val difference = prescribed - dispensed

            FullTimeSeriesEntry(
                period = period,
                prescribed = prescribed,
                dispensed = dispensed,
                difference = difference
            )
        }

        return EreceptFullTimeSeriesResponse(
            aggregationType = request.aggregationType,
            calculationMode = request.calculationMode,
            normalisationMode = request.normalisationMode,
            timeGranularity = request.timeGranularity,
            district = request.district,
            series = series,
            includedMedicineProducts = included.map { MedicinalProductIdentificators(it.id!!, it.suklCode) },
            ignoredMedicineProducts = ignored.map { MedicinalProductIdentificators(it.id!!, it.suklCode) }
        )
    }

    private fun aggregate(
        rows: List<EReceptDistrictDataRow>,
        aggregationType: EreceptType,
        extractor: (EReceptDistrictDataRow) -> Pair<Int, Int>
    ): Map<String, Int> {
        return rows.associate {
            val (prescribed, dispensed) = extractor(it)
            val value = when (aggregationType) {
                EreceptType.PRESCRIBED -> prescribed
                EreceptType.DISPENSED -> dispensed
                EreceptType.DIFFERENCE -> prescribed - dispensed
            }
            it.districtCode to value
        }
    }

    private fun aggregateAndNormalize(
        rows: List<EReceptDistrictDataRow>,
        aggregationType: EreceptType,
        extractor: (EReceptDistrictDataRow) -> Pair<Int, Int>
    ): Map<String, Int> {
        return rows.associate {
            val (prescribed, dispensed) = extractor(it)
            val value = when (aggregationType) {
                EreceptType.PRESCRIBED -> prescribed
                EreceptType.DISPENSED -> dispensed
                EreceptType.DIFFERENCE -> prescribed - dispensed
            }
            it.districtCode to normalize(value, it.population)
        }
    }

    private fun aggregateWithDDD(
        rows: List<EReceptDistrictDataRow>,
        includedProducts: List<MpdMedicinalProduct>,
        aggregationType: EreceptType
    ): Map<String, Int> {
        val dddMap = includedProducts.associateBy({ it.id!! }, { it.dailyDosePackaging!! })

        return rows
            .groupBy { it.districtCode }
            .mapValues { (_, rowsInDistrict) ->
                rowsInDistrict.sumOf {
                    val ddd = dddMap[it.medicinalProductId] ?: BigDecimal.ZERO
                    val raw = when (aggregationType) {
                        EreceptType.PRESCRIBED -> it.prescribed
                        EreceptType.DISPENSED -> it.dispensed
                        EreceptType.DIFFERENCE -> it.prescribed - it.dispensed
                    }
                    (BigDecimal(raw) * ddd).toInt()
                }
            }
    }

    private fun aggregateAndNormalizeWithDDD(
        rows: List<EReceptDistrictDataRow>,
        includedProducts: List<MpdMedicinalProduct>,
        aggregationType: EreceptType
    ): Map<String, Int> {
        val dddValues = aggregateWithDDD(rows, includedProducts, aggregationType)
        return dddValues.mapValues { (district, value) ->
            val population = rows.find { it.districtCode == district }?.population ?: 0
            normalize(value, population)
        }
    }

    private fun normalize(value: Int, population: Int): Int {
        return if (population <= 0) 0 else (value / (population / 1000.0)).roundToInt()
    }

    private fun calculateSummaryFromRows(rows: List<EReceptDistrictDataRow>): SummaryValues {
        val prescribed = rows.sumOf { it.prescribed }
        val dispensed = rows.sumOf { it.dispensed }
        val difference = prescribed - dispensed
        val percentage = if (prescribed == 0) 0.0 else (difference.toDouble() / prescribed) * 100
        return SummaryValues(prescribed, dispensed, difference, percentage)
    }

    private fun calculateSummaryFromRawRows(rows: List<EReceptMonthlyDistrictAggregate>): SummaryValues {
        val prescribed = rows.sumOf { it.prescribed }
        val dispensed = rows.sumOf { it.dispensed }
        val difference = prescribed - dispensed
        val percentage = if (prescribed == 0) 0.0 else (difference.toDouble() / prescribed) * 100
        return SummaryValues(prescribed, dispensed, difference, percentage)
    }

    private fun aggregateMonthly(
        rows: List<EReceptMonthlyDistrictAggregate>,
        aggregationType: EreceptType,
        extractor: (EReceptMonthlyDistrictAggregate) -> Pair<Int, Int>
    ): Map<String, Int> {
        return rows.associate {
            val (prescribed, dispensed) = extractor(it)
            val value = when (aggregationType) {
                EreceptType.PRESCRIBED -> prescribed
                EreceptType.DISPENSED -> dispensed
                EreceptType.DIFFERENCE -> prescribed - dispensed
            }
            it.districtCode to value
        }
    }

    private fun aggregateMonthlyAndNormalize(
        rows: List<EReceptMonthlyDistrictAggregate>,
        aggregationType: EreceptType,
        extractor: (EReceptMonthlyDistrictAggregate) -> Pair<Int, Int>
    ): Map<String, Int> {
        return rows.associate {
            val (prescribed, dispensed) = extractor(it)
            val value = when (aggregationType) {
                EreceptType.PRESCRIBED -> prescribed
                EreceptType.DISPENSED -> dispensed
                EreceptType.DIFFERENCE -> prescribed - dispensed
            }
            it.districtCode to normalize(value, it.population)
        }
    }

    private fun aggregateMonthlyWithDDD(
        rows: List<EReceptMonthlyDistrictAggregate>,
        includedProducts: List<MpdMedicinalProduct>,
        aggregationType: EreceptType
    ): Map<String, Int> {
        val dddMap = includedProducts.associateBy({ it.id!! }, { it.dailyDosePackaging!! })

        return rows
            .groupBy { it.districtCode }
            .mapValues { (_, rowsInDistrict) ->
                rowsInDistrict.sumOf {
                    val ddd = dddMap[it.medicinalProductId] ?: BigDecimal.ZERO
                    val raw = when (aggregationType) {
                        EreceptType.PRESCRIBED -> it.prescribed
                        EreceptType.DISPENSED -> it.dispensed
                        EreceptType.DIFFERENCE -> it.prescribed - it.dispensed
                    }
                    (BigDecimal(raw) * ddd).toInt()
                }
            }
    }

    private fun aggregateMonthlyAndNormalizeWithDDD(
        rows: List<EReceptMonthlyDistrictAggregate>,
        includedProducts: List<MpdMedicinalProduct>,
        aggregationType: EreceptType
    ): Map<String, Int> {
        val dddValues = aggregateMonthlyWithDDD(rows, includedProducts, aggregationType)
        return dddValues.mapValues { (district, value) ->
            val population = rows.find { it.districtCode == district }?.population ?: 0
            normalize(value, population)
        }
    }
}
