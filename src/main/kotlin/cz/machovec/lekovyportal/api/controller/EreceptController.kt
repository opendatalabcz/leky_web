package cz.machovec.lekovyportal.api.controller

import cz.machovec.lekovyportal.api.model.PrescriptionDispenseByDistrictAggregateRequest
import cz.machovec.lekovyportal.api.model.PrescriptionDispenseByDistrictAggregateResponse
import cz.machovec.lekovyportal.api.model.FullTimeSeriesRequest
import cz.machovec.lekovyportal.api.model.FullTimeSeriesResponse
import cz.machovec.lekovyportal.api.model.PrescriptionDispenseByDistrictTimeSeriesRequest
import cz.machovec.lekovyportal.api.model.PrescriptionDispenseByDistrictTimeSeriesResponse
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
    fun getAggregatedByDistrict(@RequestBody request: PrescriptionDispenseByDistrictAggregateRequest): PrescriptionDispenseByDistrictAggregateResponse {
        logger.info(
            "District map aggregation — aggregationType=${request.aggregationType}, " +
                    "ids=${request.medicinalProductIds.joinToString(",")}, " +
                    "regNumbers=${request.registrationNumbers.joinToString(",")}, " +
                    "dateFrom=${request.dateFrom}, dateTo=${request.dateTo}, " +
                    "calculationMode=${request.calculationMode}, " +
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
        @RequestBody request: PrescriptionDispenseByDistrictTimeSeriesRequest
    ): PrescriptionDispenseByDistrictTimeSeriesResponse {
        logger.info(
            "eRecept district time series — aggregationType=${request.aggregationType}, " +
                    "ids=${request.medicinalProductIds.joinToString(",")}, " +
                    "from=${request.dateFrom} to=${request.dateTo}, " +
                    "calculationMode=${request.calculationMode}, normalisation=${request.normalisationMode}"
        )

        val startedAt = System.currentTimeMillis()

        val response = ereceptService.aggregateSeriesByDistrict(request)

        val durationMs = System.currentTimeMillis() - startedAt
        logger.info("TIME-SERIES BY DISTRICT: $durationMs ms")

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

        val startedAt = System.currentTimeMillis()

        val response = ereceptService.getFullTimeSeries(request)

        val durationMs = System.currentTimeMillis() - startedAt
        logger.info("TIME-SERIES: $durationMs ms")

        return response
    }
}
