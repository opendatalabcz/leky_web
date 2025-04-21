package cz.machovec.lekovyportal.api

import cz.machovec.lekovyportal.api.dto.PrescriptionDispenseByDistrictTimeSeriesRequest
import cz.machovec.lekovyportal.api.dto.PrescriptionDispenseByDistrictTimeSeriesResponse
import cz.machovec.lekovyportal.service.DistrictDataService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/erecept/prescription-dispense/by-district")
class EReceptController(
    private val districtDataService: DistrictDataService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/time-series")
    fun getTimeSeries(
        @RequestBody request: PrescriptionDispenseByDistrictTimeSeriesRequest
    ): PrescriptionDispenseByDistrictTimeSeriesResponse {
        logger.info(
            "eRecept district time series — aggregationType=${request.aggregationType}, " +
                    "ids=${request.medicinalProductIds.joinToString(",")}, " +
                    "from=${request.dateFrom} to=${request.dateTo}, " +
                    "calculationMode=${request.calculationMode}, normalisation=${request.normalisationMode}"
        )

        val response = districtDataService.aggregateSeriesByDistrict(request)

        logger.info("Returning district series for ${response.series.size} months")
        return response
    }
}
