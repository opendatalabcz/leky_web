package cz.machovec.lekovyportal.api.controller

import cz.machovec.lekovyportal.api.model.SubstanceResponse
import cz.machovec.lekovyportal.api.service.SubstanceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/substances")
class SubstanceController(
    private val substanceService: SubstanceService
) {
    @GetMapping
    fun searchSubstances(@RequestParam query: String): List<SubstanceResponse> {
        return substanceService.searchSubstances(query)
    }
}
