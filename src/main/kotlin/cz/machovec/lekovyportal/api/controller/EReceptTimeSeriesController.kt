package cz.machovec.lekovyportal.api.controller

import cz.machovec.lekovyportal.api.model.FullTimeSeriesRequest
import cz.machovec.lekovyportal.api.model.FullTimeSeriesResponse
import cz.machovec.lekovyportal.api.service.EReceptTimeSeriesService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/erecept/prescription-dispense/time-series")
class EReceptTimeSeriesController(
    private val eReceptTimeSeriesService: EReceptTimeSeriesService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/full")
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

