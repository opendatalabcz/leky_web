package cz.machovec.lekovyportal.api

import cz.machovec.lekovyportal.api.dto.DistrictDataRequest
import cz.machovec.lekovyportal.service.DistrictDataService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/district-data")
class DistrictDataController(
    private val districtDataService: DistrictDataService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun getDistrictAggregates(@RequestBody request: DistrictDataRequest): Map<String, Int> {
        logger.info(
            "District map aggregation — type=${request.filterType}, ids=${request.medicinalProductIds.joinToString(",")}, " +
                    "dateFrom=${request.dateFrom}, dateTo=${request.dateTo}"
        )

        val data = districtDataService.aggregateByDistrict(request)

        logger.info("Returning district aggregates: ${data.entries.joinToString { "${it.key}=${it.value}" }}")

        return data
    }
}
