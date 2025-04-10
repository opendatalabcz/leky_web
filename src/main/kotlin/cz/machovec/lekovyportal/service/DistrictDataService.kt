package cz.machovec.lekovyportal.service

import cz.machovec.lekovyportal.api.dto.DistrictDataRequest
import cz.machovec.lekovyportal.api.dto.EReceptDistrictDataResponse
import cz.machovec.lekovyportal.api.dto.MedicineProductInfo
import cz.machovec.lekovyportal.api.enum.CalculationMode
import cz.machovec.lekovyportal.api.enum.EReceptDataTypeAggregation
import cz.machovec.lekovyportal.api.enum.NormalisationMode
import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProduct
import cz.machovec.lekovyportal.domain.repository.erecept.EReceptDistrictDataRow
import cz.machovec.lekovyportal.domain.repository.erecept.EReceptRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMedicinalProductRepository
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
    fun aggregateByDistrict(request: DistrictDataRequest): EReceptDistrictDataResponse {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
        val from: YearMonth? = request.dateFrom?.let { YearMonth.parse(it, formatter) }
        val to: YearMonth? = request.dateTo?.let { YearMonth.parse(it, formatter) }

        val allProducts = medicinalProductRepository.findAllByIdIn(request.medicinalProductIds)
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
            NormalisationMode.PER_1000 -> when (request.calculationMode) {
                CalculationMode.UNITS ->
                    aggregateAndNormalize(rows, request.aggregationType) { it.prescribed to it.dispensed }
                CalculationMode.DAILY_DOSES ->
                    aggregateAndNormalizeWithDDD(rows, included, request.aggregationType)
            }
            NormalisationMode.ABSOLUTE -> when (request.calculationMode) {
                CalculationMode.UNITS ->
                    aggregate(rows, request.aggregationType) { it.prescribed to it.dispensed }
                CalculationMode.DAILY_DOSES ->
                    aggregateWithDDD(rows, included, request.aggregationType)
            }
        }

        return EReceptDistrictDataResponse(
            aggregationType = request.aggregationType,
            calculationMode = request.calculationMode,
            normalisationMode = request.normalisationMode,
            dateFrom = request.dateFrom,
            dateTo = request.dateTo,
            districtValues = districtValues,
            includedMedicineProducts = included.map { MedicineProductInfo(it.id!!, it.suklCode) },
            ignoredMedicineProducts = ignored.map { MedicineProductInfo(it.id!!, it.suklCode) }
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
}
