package cz.machovec.lekovyportal.api

import cz.machovec.lekovyportal.api.dto.MedicinalProductGroupedByRegNumberResponse
import cz.machovec.lekovyportal.api.dto.MedicinalProductResponse
import cz.machovec.lekovyportal.service.MedicinalProductService
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
    ): List<MedicinalProductResponse> {
        logger.info("Searching medicinal products: atcGroupId=$atcGroupId, substanceId=$substanceId, query=$query")
        return medicinalProductService.searchMedicinalProducts(atcGroupId, substanceId, query)
    }

    @GetMapping("/grouped-by-reg-number")
    fun searchMedicinalProductsGroupedByRegNumber(
        @RequestParam(required = false) atcGroupId: Long?,
        @RequestParam(required = false) substanceId: Long?,
        @RequestParam(required = false) query: String?
    ): List<MedicinalProductGroupedByRegNumberResponse> {
        logger.info("Searching medicinal products grouped by registration number: atcGroupId=$atcGroupId, substanceId=$substanceId, query=$query")
        return medicinalProductService.searchMedicinalProductsGroupedByRegNumber(atcGroupId, substanceId, query)
    }

    @GetMapping("/by-ids")
    fun getMedicinalProductsByIds(
        @RequestParam ids: List<Long>
    ): List<MedicinalProductResponse> {
        logger.info("CART - fetching medicinal products by IDs: ${ids.joinToString(", ")}")
        return medicinalProductService.findByIds(ids)
    }
}
