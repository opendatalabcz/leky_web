package cz.machovec.lekovyportal.api

import cz.machovec.lekovyportal.api.dto.DistrictDataRequest
import cz.machovec.lekovyportal.api.dto.EReceptDistrictDataResponse
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
    fun getDistrictAggregates(@RequestBody request: DistrictDataRequest): EReceptDistrictDataResponse {
        logger.info(
            "District map aggregation — aggregationType=${request.aggregationType}, " +
                    "ids=${request.medicinalProductIds.joinToString(",")}, " +
                    "dateFrom=${request.dateFrom}, dateTo=${request.dateTo}, " +
                    "calculationMode=${request.calculationMode}, " +
                    "normalisationMode=${request.normalisationMode}"
        )

        val response = districtDataService.aggregateByDistrict(request)

        logger.info("Returning district data: ${response.districtValues.entries.joinToString { "${it.key}=${it.value}" }}")
        return response
    }
}
