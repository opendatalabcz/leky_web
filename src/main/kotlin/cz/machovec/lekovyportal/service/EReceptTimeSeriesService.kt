package cz.machovec.lekovyportal.service

import cz.machovec.lekovyportal.api.dto.FullTimeSeriesEntry
import cz.machovec.lekovyportal.api.dto.FullTimeSeriesRequest
import cz.machovec.lekovyportal.api.dto.FullTimeSeriesResponse
import cz.machovec.lekovyportal.api.dto.Granularity
import cz.machovec.lekovyportal.api.dto.MedicineProductInfo
import cz.machovec.lekovyportal.api.enum.CalculationMode
import cz.machovec.lekovyportal.domain.repository.erecept.EReceptRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMedicinalProductRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class EReceptTimeSeriesService(
    private val ereceptRepository: EReceptRepository,
    private val medicinalProductRepository: MpdMedicinalProductRepository
) {
    fun getFullTimeSeries(request: FullTimeSeriesRequest): FullTimeSeriesResponse {
        val allProducts = medicinalProductRepository.findAllByIdIn(request.medicinalProductIds)
        val (included, ignored) = allProducts.partition {
            request.calculationMode != CalculationMode.DAILY_DOSES ||
                    (it.dailyDosePackaging != null && it.dailyDosePackaging > BigDecimal.ZERO)
        }

        val rawData = ereceptRepository.findRawMonthlyAggregates(
            medicinalProductIds = included.mapNotNull { it.id }
        )

        val filtered = rawData.filter { request.district == null || it.districtCode == request.district }

        val grouped = when (request.granularity) {
            Granularity.MONTH -> filtered.groupBy { "%04d-%02d".format(it.year, it.month) }
            Granularity.YEAR -> filtered.groupBy { it.year.toString() }
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

        return FullTimeSeriesResponse(
            aggregationType = request.aggregationType,
            calculationMode = request.calculationMode,
            normalisationMode = request.normalisationMode,
            granularity = request.granularity,
            district = request.district,
            series = series,
            includedMedicineProducts = included.map { MedicineProductInfo(it.id!!, it.suklCode) },
            ignoredMedicineProducts = ignored.map { MedicineProductInfo(it.id!!, it.suklCode) }
        )
    }
}
