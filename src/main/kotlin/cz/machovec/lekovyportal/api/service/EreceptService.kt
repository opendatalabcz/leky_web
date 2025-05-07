package cz.machovec.lekovyportal.api.service

import EreceptAggregateByDistrictRequest
import EreceptAggregateByDistrictResponse
import EreceptFullTimeSeriesRequest
import EreceptFullTimeSeriesResponse
import EreceptFullTimeSeriesEntry
import EreceptTimeSeriesByDistrictRequest
import EreceptTimeSeriesByDistrictResponse
import SummaryValues
import TimeSeriesMonthDistrictValues
import cz.machovec.lekovyportal.api.model.mpd.MedicinalProductIdentificators
import cz.machovec.lekovyportal.api.logic.DistrictAggregator
import cz.machovec.lekovyportal.api.logic.DoseUnitConverter
import cz.machovec.lekovyportal.api.model.enums.MedicinalUnitMode
import cz.machovec.lekovyportal.api.model.enums.TimeGranularity
import cz.machovec.lekovyportal.core.domain.mpd.MpdMedicinalProduct
import cz.machovec.lekovyportal.core.repository.erecept.*
import cz.machovec.lekovyportal.core.repository.mpd.MpdMedicinalProductRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Service
class EreceptService(
    private val ereceptRepository: EreceptRepository,
    private val medicinalProductRepository: MpdMedicinalProductRepository
) {

    private val ymFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    /* ---------- PUBLIC API ---------- */

    fun getAggregatedByDistrict(
        request: EreceptAggregateByDistrictRequest
    ): EreceptAggregateByDistrictResponse {

        // Step 1: Load and filter products according to DDD compatibility
        val (included, ignored) = loadAndFilterProducts(
            request.medicinalProductIds,
            request.registrationNumbers,
            request.medicinalUnitMode
        )

        // Step 2: Query repository
        val rows = ereceptRepository.findAggregatesAllDistricts(
            medicinalProductIds = included.mapNotNull { it.id },
            dateFrom = request.dateFrom?.let { YearMonth.parse(it, ymFormatter) },
            dateTo = request.dateTo?.let { YearMonth.parse(it, ymFormatter) }
        )

        // Step 3: Calculate aggregated values
        val aggregator = newAggregator(included)
        val districtValues = aggregator.aggregateDistrictRows(
            rows = rows,
            aggType = request.aggregationType,
            unitMode = request.medicinalUnitMode,
            normMode = request.normalisationMode
        )

        // Step 4: Build DTO response
        return EreceptAggregateByDistrictResponse(
            aggregationType = request.aggregationType,
            medicinalUnitMode = request.medicinalUnitMode,
            normalisationMode = request.normalisationMode,
            dateFrom = request.dateFrom,
            dateTo = request.dateTo,
            districtValues = districtValues,
            includedMedicineProducts = included.toIdDto(),
            ignoredMedicineProducts = ignored.toIdDto(),
            summary = rows.toSummary()
        )
    }

    fun getTimeSeriesByDistrict(
        request: EreceptTimeSeriesByDistrictRequest
    ): EreceptTimeSeriesByDistrictResponse {

        // Step 1: Load and filter products
        val (included, ignored) = loadAndFilterProducts(
            request.medicinalProductIds,
            request.registrationNumbers,
            request.medicinalUnitMode
        )

        // Step 2: Query repository
        val raw = ereceptRepository.findMonthlyAllDistricts(
            medicinalProductIds = included.mapNotNull { it.id },
            dateFrom = YearMonth.parse(request.dateFrom, ymFormatter),
            dateTo = YearMonth.parse(request.dateTo, ymFormatter)
        )

        // Step 3: Calculate month-by-month aggregates
        val aggregator = newAggregator(included)
        val rowsByMonth = raw.groupBy { "%04d-%02d".format(it.year, it.month) }

        val series = rowsByMonth.toSortedMap().map { (period, rowsForMonth) ->
            val districtMap = aggregator.aggregateMonthlyDistrictRows(
                rows = rowsForMonth,
                aggType = request.aggregationType,
                unitMode = request.medicinalUnitMode,
                normMode = request.normalisationMode
            )

            TimeSeriesMonthDistrictValues(
                month = period,
                districtValues = districtMap,
                summary = rowsForMonth.toSummary()
            )
        }

        // Step 4: Build DTO response
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

        // Step 1: Load and filter products
        val (included, ignored) = loadAndFilterProducts(
            request.medicinalProductIds,
            request.registrationNumbers,
            request.medicinalUnitMode
        )

        // Step 2: Query repository
        val raw = ereceptRepository.findFullMonthly(
            medicinalProductIds = included.mapNotNull { it.id }
        ).filter { request.district == null || it.districtCode == request.district }

        // Step 3: Convert packages ↔ DDD
        val converter = DoseUnitConverter(
            included.associate { it.id!! to (it.dailyDosePackaging ?: BigDecimal.ZERO) }
        )

        val monthly = raw.map {
            val prescribed = converter.convert(it.medicinalProductId, it.prescribed.toLong(), request.medicinalUnitMode)
            val dispensed = converter.convert(it.medicinalProductId, it.dispensed.toLong(), request.medicinalUnitMode)
            it.copy(prescribed = prescribed.toInt(), dispensed = dispensed.toInt())
        }

        // Step 4: Group by month or year
        val grouped = when (request.timeGranularity) {
            TimeGranularity.MONTH -> monthly.groupBy { "%04d-%02d".format(it.year, it.month) }
            TimeGranularity.YEAR -> monthly.groupBy { it.year.toString() }
        }

        val series = grouped.toSortedMap().map { (period, rows) ->
            val prescribed = rows.sumOf { it.prescribed }
            val dispensed = rows.sumOf { it.dispensed }
            EreceptFullTimeSeriesEntry(
                period = period,
                prescribed = prescribed,
                dispensed = dispensed,
                difference = prescribed - dispensed
            )
        }

        // Step 5: Build DTO response
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

    /* ---------- PRIVATE UTILITIES ---------- */

    private fun loadAndFilterProducts(
        ids: List<Long>,
        regNumbers: List<String>,
        unitMode: MedicinalUnitMode
    ): Pair<List<MpdMedicinalProduct>, List<MpdMedicinalProduct>> {
        val byId = if (ids.isNotEmpty()) medicinalProductRepository.findAllByIdIn(ids) else emptyList()
        val byReg = if (regNumbers.isNotEmpty()) medicinalProductRepository.findAllByRegistrationNumberIn(regNumbers) else emptyList()
        val all = (byId + byReg).distinctBy { it.id }

        return all.partition { prod ->
            unitMode != MedicinalUnitMode.DAILY_DOSES ||
                    (prod.dailyDosePackaging != null && prod.dailyDosePackaging > BigDecimal.ZERO)
        }
    }

    private fun newAggregator(included: List<MpdMedicinalProduct>) =
        DistrictAggregator(
            DoseUnitConverter(
                included.associate { it.id!! to (it.dailyDosePackaging ?: BigDecimal.ZERO) }
            )
        )

    private fun List<EReceptDistrictDataRow>.toSummary(): SummaryValues {
        val prescribedSum = sumOf { it.prescribed }
        val dispensedSum = sumOf { it.dispensed }
        val difference = prescribedSum - dispensedSum
        val percentage = if (prescribedSum == 0) 0.0 else (difference.toDouble() / prescribedSum) * 100

        return SummaryValues(
            prescribed = prescribedSum,
            dispensed = dispensedSum,
            difference = difference,
            percentageDifference = percentage
        )
    }

    private fun List<EReceptMonthlyDistrictAggregate>.toSummary(): SummaryValues {
        val prescribedSum = sumOf { it.prescribed }
        val dispensedSum = sumOf { it.dispensed }
        val difference = prescribedSum - dispensedSum
        val percentage = if (prescribedSum == 0) 0.0 else (difference.toDouble() / prescribedSum) * 100

        return SummaryValues(
            prescribed = prescribedSum,
            dispensed = dispensedSum,
            difference = difference,
            percentageDifference = percentage
        )
    }

    private fun List<MpdMedicinalProduct>.toIdDto() =
        map { MedicinalProductIdentificators(it.id!!, it.suklCode) }
}
