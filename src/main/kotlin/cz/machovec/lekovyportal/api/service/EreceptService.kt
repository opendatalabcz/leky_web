package cz.machovec.lekovyportal.api.service

import EreceptAggregateByDistrictRequest
import EreceptAggregateByDistrictResponse
import EreceptFullTimeSeriesEntry
import EreceptFullTimeSeriesRequest
import EreceptFullTimeSeriesResponse
import EreceptTimeSeriesByDistrictRequest
import EreceptTimeSeriesByDistrictResponse
import TimeSeriesMonthDistrictValues
import cz.machovec.lekovyportal.api.calculations.DistrictAggregator
import cz.machovec.lekovyportal.api.calculations.SummaryCalculator
import cz.machovec.lekovyportal.api.model.enums.MedicinalUnitMode
import cz.machovec.lekovyportal.api.model.enums.TimeGranularity
import cz.machovec.lekovyportal.api.model.mpd.MedicinalProductIdentificators
import cz.machovec.lekovyportal.core.domain.mpd.MpdMedicinalProduct
import cz.machovec.lekovyportal.core.repository.erecept.EreceptRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdMedicinalProductRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

@Service
class EreceptService(
    private val ereceptRepository: EreceptRepository,
    private val medicinalProductRepository: MpdMedicinalProductRepository,
    private val districtAggregator: DistrictAggregator,
    private val summaryCalculator: SummaryCalculator
) {

    private val ymFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    /** PUBLIC API **/

    fun getAggregatedByDistrict(
        request: EreceptAggregateByDistrictRequest
    ): EreceptAggregateByDistrictResponse {
        // Step 1: Load and filter medicinal products
        val (included, ignored) = loadAndFilterProducts(
            request.medicinalProductIds,
            request.registrationNumbers,
            request.medicinalUnitMode
        )

        // Step 2: Query aggregated data per district
        val rows = ereceptRepository.getAggregatedByDistrictRows(
            medicinalProductIds = included.mapNotNull { it.id },
            dateFrom = request.dateFrom?.let { YearMonth.parse(it, ymFormatter) },
            dateTo = request.dateTo?.let { YearMonth.parse(it, ymFormatter) }
        )

        val dddPerProduct = included.associate { it.id!! to (it.dailyDosePackaging ?: BigDecimal.ZERO) }

        // Step 3: Aggregate district values
        val districtValues = districtAggregator.aggregate(
            rows = rows,
            aggType = request.aggregationType,
            unitMode = request.medicinalUnitMode,
            normMode = request.normalisationMode,
            dddPerProduct = dddPerProduct
        )

        // Step 4: Calculate summary values
        val summary = summaryCalculator.fromDistrictRows(rows)

        return EreceptAggregateByDistrictResponse(
            aggregationType = request.aggregationType,
            medicinalUnitMode = request.medicinalUnitMode,
            normalisationMode = request.normalisationMode,
            dateFrom = request.dateFrom,
            dateTo = request.dateTo,
            districtValues = districtValues,
            includedMedicineProducts = included.toIdDto(),
            ignoredMedicineProducts = ignored.toIdDto(),
            summary = summary
        )
    }

    fun getTimeSeriesByDistrict(
        request: EreceptTimeSeriesByDistrictRequest
    ): EreceptTimeSeriesByDistrictResponse {
        // Step 1: Load and filter medicinal products
        val (included, ignored) = loadAndFilterProducts(
            request.medicinalProductIds,
            request.registrationNumbers,
            request.medicinalUnitMode
        )

        // Step 2: Query aggregated data per district
        val raw = ereceptRepository.getTimeSeriesByDistrictRows(
            medicinalProductIds = included.mapNotNull { it.id },
            dateFrom = YearMonth.parse(request.dateFrom, ymFormatter),
            dateTo = YearMonth.parse(request.dateTo, ymFormatter)
        )

        // Step 3: Group and aggregate monthly series
        val dddPerProduct = included.associate { it.id!! to (it.dailyDosePackaging ?: BigDecimal.ZERO) }
        val rowsByMonth = raw.groupBy { "%04d-%02d".format(it.year, it.month) }

        val series = rowsByMonth.toSortedMap().map { (period, rowsForMonth) ->
            val districtMap = districtAggregator.aggregateMonthly(
                rows = rowsForMonth,
                aggType = request.aggregationType,
                unitMode = request.medicinalUnitMode,
                normMode = request.normalisationMode,
                dddPerProduct = dddPerProduct
            )

            TimeSeriesMonthDistrictValues(
                month = period,
                districtValues = districtMap,
                summary = summaryCalculator.fromMonthlyRows(rowsForMonth)
            )
        }

        return EreceptTimeSeriesByDistrictResponse(
            aggregationType = request.aggregationType,
            medicinalUnitMode = request.medicinalUnitMode,
            normalisationMode = request.normalisationMode,
            dateFrom = request.dateFrom,
            dateTo = request.dateTo,
            series = series,
            includedMedicineProducts = included.toIdDto(),
            ignoredMedicineProducts = ignored.toIdDto()
        )
    }

    fun getFullTimeSeries(
        request: EreceptFullTimeSeriesRequest
    ): EreceptFullTimeSeriesResponse {
        // Step 1: Load and filter medicinal products
        val (included, ignored) = loadAndFilterProducts(
            request.medicinalProductIds,
            request.registrationNumbers,
            request.medicinalUnitMode
        )

        // Step 2: Query raw data
        val raw = ereceptRepository.getFullTimeSeriesRows(
            medicinalProductIds = included.mapNotNull { it.id },
            districtCode = request.district
        )

        // Step 3: Transformation and grouping
        val dddPerProduct = included.associate { it.id!! to (it.dailyDosePackaging ?: BigDecimal.ZERO) }
        val monthly = raw.map {
            val prescribed = districtAggregator.convertValue(it.medicinalProductId, it.prescribed, request.medicinalUnitMode, dddPerProduct)
            val dispensed = districtAggregator.convertValue(it.medicinalProductId, it.dispensed, request.medicinalUnitMode, dddPerProduct)
            it.copy(prescribed = prescribed, dispensed = dispensed)
        }

        val grouped = when (request.timeGranularity) {
            TimeGranularity.MONTH -> monthly.groupBy { "%04d-%02d".format(it.year, it.month) }
            TimeGranularity.YEAR -> monthly.groupBy { it.year.toString() }
        }

        val series = grouped.toSortedMap().map { (period, rows) ->
            val prescribed = rows.sumOf { it.prescribed }.toInt()
            val dispensed = rows.sumOf { it.dispensed }.toInt()
            EreceptFullTimeSeriesEntry(
                period = period,
                prescribed = prescribed,
                dispensed = dispensed,
                difference = prescribed - dispensed
            )
        }

        return EreceptFullTimeSeriesResponse(
            medicinalUnitMode = request.medicinalUnitMode,
            normalisationMode = request.normalisationMode,
            timeGranularity = request.timeGranularity,
            district = request.district,
            series = series,
            includedMedicineProducts = included.toIdDto(),
            ignoredMedicineProducts = ignored.toIdDto()
        )
    }

    /** PRIVATE UTILITIES **/

    private fun loadAndFilterProducts(
        ids: List<Long>,
        regNumbers: List<String>,
        unitMode: MedicinalUnitMode
    ): Pair<List<MpdMedicinalProduct>, List<MpdMedicinalProduct>> {
        val byId = if (ids.isNotEmpty()) medicinalProductRepository.findAllByIdIn(ids) else emptyList()
        val byReg = if (regNumbers.isNotEmpty()) medicinalProductRepository.findAllByRegistrationNumberIn(regNumbers) else emptyList()
        val all = (byId + byReg).distinctBy { it.id }

        return if (unitMode == MedicinalUnitMode.DAILY_DOSES) {
            all.partition { prod ->
                prod.dailyDosePackaging != null && prod.dailyDosePackaging > BigDecimal.ZERO
            }
        } else {
            all to emptyList()
        }
    }

    private fun List<MpdMedicinalProduct>.toIdDto() =
        map { MedicinalProductIdentificators(it.id!!, it.suklCode) }
}
