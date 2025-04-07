package cz.machovec.lekovyportal.service

import cz.machovec.lekovyportal.api.dto.DistrictDataRequest
import cz.machovec.lekovyportal.api.enum.EReceptFilterType
import cz.machovec.lekovyportal.api.enum.NormalisationMode
import cz.machovec.lekovyportal.domain.repository.erecept.EReceptRepository
import org.springframework.stereotype.Service

import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Service
class DistrictDataService(
    private val ereceptRepository: EReceptRepository
) {
    fun aggregateByDistrict(request: DistrictDataRequest): Map<String, Int> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
        val from: YearMonth? = request.dateFrom?.let { YearMonth.parse(it, formatter) }
        val to: YearMonth? = request.dateTo?.let { YearMonth.parse(it, formatter) }

        val rows = ereceptRepository.findAggregatesByDistrict(
            medicinalProductIds = request.medicinalProductIds,
            dateFrom = from,
            dateTo = to
        )

        fun normalize(value: Int, population: Int): Int {
            return if (population <= 0) 0 else (value / (population / 1000.0)).roundToInt()
        }

        return when (request.normalisationMode) {
            NormalisationMode.PER_1000 -> when (request.filterType) {
                EReceptFilterType.PRESCRIBED -> rows.associate { it.districtCode to normalize(it.prescribed, it.population) }
                EReceptFilterType.DISPENSED -> rows.associate { it.districtCode to normalize(it.dispensed, it.population) }
                EReceptFilterType.DIFFERENCE -> rows.associate {
                    it.districtCode to normalize(it.prescribed - it.dispensed, it.population)
                }
            }
            NormalisationMode.ABSOLUTE -> when (request.filterType) {
                EReceptFilterType.PRESCRIBED -> rows.associate { it.districtCode to it.prescribed }
                EReceptFilterType.DISPENSED -> rows.associate { it.districtCode to it.dispensed }
                EReceptFilterType.DIFFERENCE -> rows.associate { it.districtCode to (it.prescribed - it.dispensed) }
            }
        }
    }
}
