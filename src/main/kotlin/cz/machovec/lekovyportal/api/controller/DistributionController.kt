package cz.machovec.lekovyportal.api.controller

import DistributionSankeyRequest
import DistributionSankeyResponse
import DistributionTimeSeriesRequest
import DistributionTimeSeriesResponse
import cz.machovec.lekovyportal.api.service.DistributionService
import mu.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/distribution")
class DistributionController(
    private val distributionService: DistributionService,
) {

    @PostMapping("/sankey-diagram")
    fun getSankeyDiagram(
        @RequestBody request: DistributionSankeyRequest
    ): DistributionSankeyResponse {

        val response = distributionService.getSankeyDiagram(request)

        return response
    }
    @PostMapping("/time-series")
    fun getTimeSeries(
        @RequestBody request: DistributionTimeSeriesRequest
    ): DistributionTimeSeriesResponse {

        val response = distributionService.getTimeSeries(request)

        return response
    }
}
