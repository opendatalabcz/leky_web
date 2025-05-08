package cz.machovec.lekovyportal.api.controller

import cz.machovec.lekovyportal.api.model.mpd.MedicinalProductGroupedByRegNumberResponse
import cz.machovec.lekovyportal.api.model.mpd.MedicinalProductResponse
import cz.machovec.lekovyportal.api.model.PagedResponse
import cz.machovec.lekovyportal.api.service.MedicinalProductService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import mu.KotlinLogging

@RestController
@RequestMapping("/api/medicinal-products")
class MedicinalProductController(
    private val medicinalProductService: MedicinalProductService
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping
    fun search(
        @RequestParam(required = false) atcGroupId: Long?,
        @RequestParam(required = false) substanceId: Long?,
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int
    ): PagedResponse<MedicinalProductResponse> {
        logger.info { "Searching medicinal products with params: atcGroupId=$atcGroupId, substanceId=$substanceId, query=$query, page=$page, size=$size" }

        return medicinalProductService.search(atcGroupId, substanceId, query, page, size)
    }

    @GetMapping("/grouped-by-reg-number")
    fun searchGroupedByRegNumber(
        @RequestParam(required = false) atcGroupId: Long?,
        @RequestParam(required = false) substanceId: Long?,
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int
    ): PagedResponse<MedicinalProductGroupedByRegNumberResponse> {
        return medicinalProductService.searchGroupedByRegNumber(atcGroupId, substanceId, query, page, size)
    }

    @GetMapping("/by-ids")
    fun findByIds(
        @RequestParam ids: List<Long>
    ): List<MedicinalProductResponse> {
        return medicinalProductService.findByIds(ids)
    }

    @GetMapping("/grouped-by-reg-numbers")
    fun findGroupedByRegNumbers(
        @RequestParam regNumbers: List<String>
    ): List<MedicinalProductGroupedByRegNumberResponse> {
        return medicinalProductService.findGroupedByRegNumbers(regNumbers)
    }
}
