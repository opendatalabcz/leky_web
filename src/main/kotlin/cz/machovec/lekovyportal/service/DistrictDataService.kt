package cz.machovec.lekovyportal.service

import cz.machovec.lekovyportal.api.dto.DistrictDataRequest
import cz.machovec.lekovyportal.api.enum.EReceptFilterType
import cz.machovec.lekovyportal.domain.repository.erecept.EReceptRepository
import org.springframework.stereotype.Service

import java.time.YearMonth
import java.time.format.DateTimeFormatter

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

        return when (request.filterType) {
            EReceptFilterType.PRESCRIBED -> rows.associate { it.districtCode to it.prescribed }
            EReceptFilterType.DISPENSED -> rows.associate { it.districtCode to it.dispensed }
            EReceptFilterType.DIFFERENCE -> rows.associate { it.districtCode to (it.prescribed - it.dispensed) }
        }
    }
}
