package cz.machovec.lekovyportal.api.service

import cz.machovec.lekovyportal.api.model.PrescriptionDispenseByDistrictAggregateRequest
import cz.machovec.lekovyportal.api.model.PrescriptionDispenseByDistrictAggregateResponse
import cz.machovec.lekovyportal.api.model.MedicinalProductIdentificators
import cz.machovec.lekovyportal.api.model.PrescriptionDispenseByDistrictTimeSeriesRequest
import cz.machovec.lekovyportal.api.model.PrescriptionDispenseByDistrictTimeSeriesResponse
import cz.machovec.lekovyportal.api.model.SummaryValues
import cz.machovec.lekovyportal.api.model.TimeSeriesMonthDistrictValues
import cz.machovec.lekovyportal.api.model.enums.CalculationMode
import cz.machovec.lekovyportal.api.model.enums.EReceptDataTypeAggregation
import cz.machovec.lekovyportal.api.model.enums.NormalisationMode
import cz.machovec.lekovyportal.core.domain.mpd.MpdMedicinalProduct
import cz.machovec.lekovyportal.core.repository.erecept.EReceptDistrictDataRow
import cz.machovec.lekovyportal.core.repository.erecept.EReceptRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdMedicinalProductRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Service
class DistrictDataService(
    private val ereceptRepository: EReceptRepository,
    private val medicinalProductRepository: MpdMedicinalProductRepository
) {
    fun aggregateByDistrict(request: PrescriptionDispenseByDistrictAggregateRequest): PrescriptionDispenseByDistrictAggregateResponse {
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

        val rows = ereceptRepository.findAggregatesByDistrict(
            medicinalProductIds = included.mapNotNull { it.id },
            dateFrom = from,
            dateTo = to
        )

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

        return PrescriptionDispenseByDistrictAggregateResponse(
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

    fun aggregateSeriesByDistrict(
        request: PrescriptionDispenseByDistrictTimeSeriesRequest
    ): PrescriptionDispenseByDistrictTimeSeriesResponse {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
        val from = YearMonth.parse(request.dateFrom, formatter)
        val to = YearMonth.parse(request.dateTo, formatter)

        val months = generateSequence(from) { current ->
            if (current < to) current.plusMonths(1) else null
        }.plus(to).toList()

        val series = mutableListOf<TimeSeriesMonthDistrictValues>()
        var includedProducts: List<MedicinalProductIdentificators> = emptyList()
        var ignoredProducts: List<MedicinalProductIdentificators> = emptyList()

        for ((index, month) in months.withIndex()) {
            val snapshot = aggregateByDistrict(
                PrescriptionDispenseByDistrictAggregateRequest(
                    dateFrom = month.format(formatter),
                    dateTo = month.format(formatter),
                    calculationMode = request.calculationMode,
                    aggregationType = request.aggregationType,
                    normalisationMode = request.normalisationMode,
                    medicinalProductIds = request.medicinalProductIds
                )
            )

            if (index == 0) {
                includedProducts = snapshot.includedMedicineProducts
                ignoredProducts = snapshot.ignoredMedicineProducts
            }

            series.add(
                TimeSeriesMonthDistrictValues(
                    month = month.format(formatter),
                    values = snapshot.districtValues,
                    summary = snapshot.summary
                )
            )
        }

        return PrescriptionDispenseByDistrictTimeSeriesResponse(
            aggregationType = request.aggregationType,
            calculationMode = request.calculationMode,
            normalisationMode = request.normalisationMode,
            dateFrom = request.dateFrom,
            dateTo = request.dateTo,
            series = series,
            includedMedicineProducts = includedProducts,
            ignoredMedicineProducts = ignoredProducts
        )
    }

    private fun aggregate(
        rows: List<EReceptDistrictDataRow>,
        aggregationType: EReceptDataTypeAggregation,
        extractor: (EReceptDistrictDataRow) -> Pair<Int, Int>
    ): Map<String, Int> {
        return rows.associate {
            val (prescribed, dispensed) = extractor(it)
            val value = when (aggregationType) {
                EReceptDataTypeAggregation.PRESCRIBED -> prescribed
                EReceptDataTypeAggregation.DISPENSED -> dispensed
                EReceptDataTypeAggregation.DIFFERENCE -> prescribed - dispensed
            }
            it.districtCode to value
        }
    }

    private fun aggregateAndNormalize(
        rows: List<EReceptDistrictDataRow>,
        aggregationType: EReceptDataTypeAggregation,
        extractor: (EReceptDistrictDataRow) -> Pair<Int, Int>
    ): Map<String, Int> {
        return rows.associate {
            val (prescribed, dispensed) = extractor(it)
            val value = when (aggregationType) {
                EReceptDataTypeAggregation.PRESCRIBED -> prescribed
                EReceptDataTypeAggregation.DISPENSED -> dispensed
                EReceptDataTypeAggregation.DIFFERENCE -> prescribed - dispensed
            }
            it.districtCode to normalize(value, it.population)
        }
    }

    private fun aggregateWithDDD(
        rows: List<EReceptDistrictDataRow>,
        includedProducts: List<MpdMedicinalProduct>,
        aggregationType: EReceptDataTypeAggregation
    ): Map<String, Int> {
        val dddMap = includedProducts.associateBy({ it.id!! }, { it.dailyDosePackaging!! })

        return rows
            .groupBy { it.districtCode }
            .mapValues { (_, rowsInDistrict) ->
                rowsInDistrict.sumOf {
                    val ddd = dddMap[it.medicinalProductId] ?: BigDecimal.ZERO
                    val raw = when (aggregationType) {
                        EReceptDataTypeAggregation.PRESCRIBED -> it.prescribed
                        EReceptDataTypeAggregation.DISPENSED -> it.dispensed
                        EReceptDataTypeAggregation.DIFFERENCE -> it.prescribed - it.dispensed
                    }
                    (BigDecimal(raw) * ddd).toInt()
                }
            }
    }

    private fun aggregateAndNormalizeWithDDD(
        rows: List<EReceptDistrictDataRow>,
        includedProducts: List<MpdMedicinalProduct>,
        aggregationType: EReceptDataTypeAggregation
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
}
