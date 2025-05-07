package cz.machovec.lekovyportal.api.controller

import cz.machovec.lekovyportal.api.model.distribution.DistributionSankeyRequest
import cz.machovec.lekovyportal.api.model.distribution.DistributionSankeyResponse
import cz.machovec.lekovyportal.api.model.erecept.Granularity
import cz.machovec.lekovyportal.api.model.mpd.MedicinalProductIdentificators
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

data class DistributionTimeSeriesRequest(
    val medicinalProductIds: List<Long>,
    val registrationNumbers: List<String>,
    val dateFrom: String, // "yyyy-MM"
    val dateTo: String,   // "yyyy-MM"
    val granularity: Granularity = Granularity.MONTH // defaultně měsíčně
)

data class DistributionTimeSeriesEntry(
    val period: String, // např. "2023-05" nebo "2023"
    val mahToDistributor: Int,
    val distributorToPharmacy: Int,
    val pharmacyToPatient: Int
)

data class DistributionTimeSeriesResponse(
    val granularity: Granularity,
    val series: List<DistributionTimeSeriesEntry>,
    val includedMedicineProducts: List<MedicinalProductIdentificators>,
    val ignoredMedicineProducts: List<MedicinalProductIdentificators>
)
