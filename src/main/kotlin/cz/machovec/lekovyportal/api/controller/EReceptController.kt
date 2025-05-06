package cz.machovec.lekovyportal.api.controller

import cz.machovec.lekovyportal.api.model.PrescriptionDispenseByDistrictAggregateRequest
import cz.machovec.lekovyportal.api.model.PrescriptionDispenseByDistrictAggregateResponse
import cz.machovec.lekovyportal.api.model.FullTimeSeriesRequest
import cz.machovec.lekovyportal.api.model.FullTimeSeriesResponse
import cz.machovec.lekovyportal.api.model.PrescriptionDispenseByDistrictTimeSeriesRequest
import cz.machovec.lekovyportal.api.model.PrescriptionDispenseByDistrictTimeSeriesResponse
import cz.machovec.lekovyportal.api.service.DistrictDataService
import cz.machovec.lekovyportal.api.service.EReceptTimeSeriesService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/erecept/prescription-dispense")
class EReceptController(
    private val districtDataService: DistrictDataService,
    private val eReceptTimeSeriesService: EReceptTimeSeriesService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/time-aggregate/by-district")
    fun getAggregatedPrescriptionDispenseByDistrict(@RequestBody request: PrescriptionDispenseByDistrictAggregateRequest): PrescriptionDispenseByDistrictAggregateResponse {
        logger.info(
            "District map aggregation — aggregationType=${request.aggregationType}, " +
                    "ids=${request.medicinalProductIds.joinToString(",")}, " +
                    "regNumbers=${request.registrationNumbers.joinToString(",")}, " +
                    "dateFrom=${request.dateFrom}, dateTo=${request.dateTo}, " +
                    "calculationMode=${request.calculationMode}, " +
                    "normalisationMode=${request.normalisationMode}"
        )

        val response = districtDataService.aggregateByDistrict(request)

        logger.info("Returning district data: ${response.districtValues.entries.joinToString { "${it.key}=${it.value}" }}")
        logger.info(
            "Summary: prescribed=${response.summary.prescribed}, " +
                    "dispensed=${response.summary.dispensed}, " +
                    "difference=${response.summary.difference}, " +
                    "percentage=${"%.1f".format(response.summary.percentageDifference)}%"
        )

        return response
    }

    @PostMapping("/time-series/by-district")
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

    @PostMapping("/time-series")
    fun getFullTimeSeries(
        @RequestBody request: FullTimeSeriesRequest
    ): FullTimeSeriesResponse {
        logger.info(
            "Fetching full time series — aggregationType=${request.aggregationType}, " +
                    "ids=${request.medicinalProductIds.joinToString(",")}, " +
                    "calculationMode=${request.calculationMode}, normalisation=${request.normalisationMode}, " +
                    "granularity=${request.granularity}, district=${request.district ?: "ALL"}"
        )

        val response = eReceptTimeSeriesService.getFullTimeSeries(request)

        logger.info("Returning time series with ${response.series.size} entries.")
        return response
    }
}
