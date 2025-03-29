package cz.machovec.lekovyportal.api

import cz.machovec.lekovyportal.api.dto.MedicinalProductResponse
import cz.machovec.lekovyportal.service.MedicinalProductService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/medicinal-products")
class MedicinalProductController(
    private val medicinalProductService: MedicinalProductService
) {

    @GetMapping
    fun searchMedicinalProducts(
        @RequestParam(required = false) atcGroupId: Long?,
        @RequestParam(required = false) substanceId: Long?,
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) period: String?
    ): List<MedicinalProductResponse> {
        println("Searching medicinal products")
        return medicinalProductService.searchMedicinalProducts(atcGroupId, substanceId, query)
    }
}
