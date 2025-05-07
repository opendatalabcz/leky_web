package cz.machovec.lekovyportal.api.controller

import EreceptAggregateByDistrictResponse
import EreceptAggregateByDistrictRequest
import EreceptFullTimeSeriesRequest
import EreceptFullTimeSeriesResponse
import EreceptTimeSeriesByDistrictRequest
import EreceptTimeSeriesByDistrictResponse
import cz.machovec.lekovyportal.api.service.EreceptService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/erecept/prescription-dispense")
class EreceptController(
    private val ereceptService: EreceptService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/time-aggregate/by-district")
    fun getAggregatedByDistrict(@RequestBody request: EreceptAggregateByDistrictRequest): EreceptAggregateByDistrictResponse {
        logger.info(
            "District map aggregation — aggregationType=${request.aggregationType}, " +
                    "ids=${request.medicinalProductIds.joinToString(",")}, " +
                    "regNumbers=${request.registrationNumbers.joinToString(",")}, " +
                    "dateFrom=${request.dateFrom}, dateTo=${request.dateTo}, " +
                    "medicinalUnitMode=${request.medicinalUnitMode}, " +
                    "normalisationMode=${request.normalisationMode}"
        )
        val startedAt = System.currentTimeMillis()

        val response = ereceptService.getAggregatedByDistrict(request)

        val durationMs = System.currentTimeMillis() - startedAt
        logger.info("TIME-AGGREGATE BY DISTRICT: $durationMs ms")

        return response
    }

    @PostMapping("/time-series/by-district")
    fun getTimeSeriesByDistrict(
        @RequestBody request: EreceptTimeSeriesByDistrictRequest
    ): EreceptTimeSeriesByDistrictResponse {
        logger.info(
            "eRecept district time series — aggregationType=${request.aggregationType}, " +
                    "ids=${request.medicinalProductIds.joinToString(",")}, " +
                    "from=${request.dateFrom} to=${request.dateTo}, " +
                    "medicinalUnitMode=${request.medicinalUnitMode}, normalisation=${request.normalisationMode}"
        )

        val startedAt = System.currentTimeMillis()

        val response = ereceptService.getTimeSeriesByDistrict(request)

        val durationMs = System.currentTimeMillis() - startedAt
        logger.info("TIME-SERIES BY DISTRICT: $durationMs ms")

        return response
    }

    @PostMapping("/time-series")
    fun getFullTimeSeries(
        @RequestBody request: EreceptFullTimeSeriesRequest
    ): EreceptFullTimeSeriesResponse {

        val startedAt = System.currentTimeMillis()

        val response = ereceptService.getFullTimeSeries(request)

        val durationMs = System.currentTimeMillis() - startedAt
        logger.info("TIME-SERIES: $durationMs ms")

        return response
    }
}
