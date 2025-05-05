package cz.machovec.lekovyportal.api.controller

import cz.machovec.lekovyportal.api.model.MedicinalProductGroupedByRegNumberResponse
import cz.machovec.lekovyportal.api.model.MedicinalProductResponse
import cz.machovec.lekovyportal.api.model.PagedResponse
import cz.machovec.lekovyportal.api.service.MedicinalProductService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/medicinal-products")
class MedicinalProductController(
    private val medicinalProductService: MedicinalProductService
) {
    private val logger = LoggerFactory.getLogger(MedicinalProductController::class.java)

    @GetMapping
    fun searchMedicinalProducts(
        @RequestParam(required = false) atcGroupId: Long?,
        @RequestParam(required = false) substanceId: Long?,
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int
    ): PagedResponse<MedicinalProductResponse> {
        return medicinalProductService.searchMedicinalProducts(atcGroupId, substanceId, query, page, size)
    }

    @GetMapping("/grouped-by-reg-number")
    fun searchMedicinalProductsGroupedByRegNumber(
        @RequestParam(required = false) atcGroupId: Long?,
        @RequestParam(required = false) substanceId: Long?,
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int
    ): PagedResponse<MedicinalProductGroupedByRegNumberResponse> {
        return medicinalProductService.searchMedicinalProductsGroupedByRegNumber(atcGroupId, substanceId, query, page, size)
    }

    @GetMapping("/by-ids")
    fun getMedicinalProductsByIds(
        @RequestParam ids: List<Long>
    ): List<MedicinalProductResponse> {
        logger.info("CART - fetching medicinal products by IDs: ${ids.joinToString(", ")}")
        return medicinalProductService.findByIds(ids)
    }

    @GetMapping("/grouped-by-reg-numbers")
    fun getGroupedMedicinalProductsByRegNumbers(
        @RequestParam regNumbers: List<String>
    ): List<MedicinalProductGroupedByRegNumberResponse> {
        logger.info("CART - fetching grouped medicinal products by regNumbers: ${regNumbers.joinToString()}")
        return medicinalProductService.findGroupedByRegNumbers(regNumbers)
    }
}
