package cz.machovec.lekovyportal.api

import cz.machovec.lekovyportal.service.EReceptService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class DistrictController(
    private val eReceptService: EReceptService,
) {
    @GetMapping("/district-data")
    fun getDistrictData(
        @RequestParam filter: String
    ): Map<String, Int> {
        val medicinalProductId = 36L
        val hardcodedYear = 2025
        val hardcodedMonth = 1

        val districtDataList = eReceptService.getDistrictData(medicinalProductId, hardcodedYear, hardcodedMonth)

        return districtDataList.associate { districtData ->
            val value = when (filter.lowercase()) {
                "prescribed" -> districtData.prescribed
                "dispensed" -> districtData.dispensed
                "difference" -> districtData.difference
                else -> throw IllegalArgumentException("Invalid filter type: $filter")
            }
            districtData.districtName to value
        }
    }
}
