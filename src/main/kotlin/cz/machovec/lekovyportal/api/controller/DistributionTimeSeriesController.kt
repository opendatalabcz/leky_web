package cz.machovec.lekovyportal.api.controller

import cz.machovec.lekovyportal.api.model.Granularity
import cz.machovec.lekovyportal.api.model.MedicineProductInfo
import cz.machovec.lekovyportal.api.service.DistributionTimeSeriesService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/distribution/time-series")
class DistributionTimeSeriesController(
    private val service: DistributionTimeSeriesService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun getTimeSeries(
        @RequestBody request: DistributionTimeSeriesRequest
    ): DistributionTimeSeriesResponse {
        logger.info(
            "Distribution time series — ids=${request.medicinalProductIds.joinToString(",")}, " +
                    "from=${request.dateFrom}, to=${request.dateTo}, granularity=${request.granularity}"
        )
        val response = service.getTimeSeries(request)
        logger.info("Returning time series with ${response.series.size} records.")
        return response
    }
}

data class DistributionTimeSeriesRequest(
    val medicinalProductIds: List<Long>,
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
    val includedMedicineProducts: List<MedicineProductInfo>,
    val ignoredMedicineProducts: List<MedicineProductInfo>
)


