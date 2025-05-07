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

    private val logger = KotlinLogging.logger {}

    @PostMapping("/sankey-diagram")
    fun getSankeyDiagram(
        @RequestBody request: DistributionSankeyRequest
    ): DistributionSankeyResponse {
        val startedAt = System.currentTimeMillis()

        val response = distributionService.getSankeyDiagram(request)

        val durationMs = System.currentTimeMillis() - startedAt
        logger.info("DISTRIBUTION - SANKEY-DIAGRAM: $durationMs ms")

        return response
    }
    @PostMapping("/time-series")
    fun getTimeSeries(
        @RequestBody request: DistributionTimeSeriesRequest
    ): DistributionTimeSeriesResponse {
        val startedAt = System.currentTimeMillis()

        val response = distributionService.getTimeSeries(request)

        val durationMs = System.currentTimeMillis() - startedAt
        logger.info("DISTRIBUTION - TIME-SERIES: $durationMs ms")

        return response
    }
}
