package cz.machovec.lekovyportal.service

import cz.machovec.lekovyportal.api.dto.DistrictDataRequest
import cz.machovec.lekovyportal.domain.repository.erecept.EreceptRepository
import org.springframework.stereotype.Service

@Service
class DistrictDataService(
    private val ereceptRepository: EreceptRepository
) {
    fun aggregateByDistrict(request: DistrictDataRequest): Map<String, Int> {
        val rows = ereceptRepository.findAggregatesByDistrict(request.medicinalProductIds)

        return when (request.filterType) {
            "prescribed" -> rows.associate { it.districtCode to it.prescribed }
            "dispensed" -> rows.associate { it.districtCode to it.dispensed }
            "difference" -> rows.associate { it.districtCode to (it.prescribed - it.dispensed) }
            else -> throw IllegalArgumentException("Unknown filter type: ${request.filterType}")
        }
    }
}

