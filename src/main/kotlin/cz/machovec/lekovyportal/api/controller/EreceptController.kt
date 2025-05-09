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

        val response = ereceptService.getAggregatedByDistrict(request)

        return response
    }

    @PostMapping("/time-series/by-district")
    fun getTimeSeriesByDistrict(
        @RequestBody request: EreceptTimeSeriesByDistrictRequest
    ): EreceptTimeSeriesByDistrictResponse {

        val response = ereceptService.getTimeSeriesByDistrict(request)

        return response
    }

    @PostMapping("/time-series")
    fun getFullTimeSeries(
        @RequestBody request: EreceptFullTimeSeriesRequest
    ): EreceptFullTimeSeriesResponse {

        val response = ereceptService.getFullTimeSeries(request)

        return response
    }
}
