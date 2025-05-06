package cz.machovec.lekovyportal.api.service

import cz.machovec.lekovyportal.api.model.FullTimeSeriesEntry
import cz.machovec.lekovyportal.api.model.FullTimeSeriesRequest
import cz.machovec.lekovyportal.api.model.FullTimeSeriesResponse
import cz.machovec.lekovyportal.api.model.Granularity
import cz.machovec.lekovyportal.api.model.MedicinalProductIdentificators
import cz.machovec.lekovyportal.api.model.enums.CalculationMode
import cz.machovec.lekovyportal.core.repository.erecept.EreceptRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdMedicinalProductRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class EReceptTimeSeriesService(
    private val ereceptRepository: EreceptRepository,
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
            includedMedicineProducts = included.map { MedicinalProductIdentificators(it.id!!, it.suklCode) },
            ignoredMedicineProducts = ignored.map { MedicinalProductIdentificators(it.id!!, it.suklCode) }
        )
    }
}
