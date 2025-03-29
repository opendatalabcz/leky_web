package cz.machovec.lekovyportal.service

import cz.machovec.lekovyportal.api.dto.MedicinalProductResponse
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMedicinalProductRepository
import org.springframework.stereotype.Service

@Service
class MedicinalProductService(
    private val medicinalProductRepository: MpdMedicinalProductRepository
) {
    fun searchMedicinalProducts(
        atcGroupId: Long?,
        substanceId: Long?,
        query: String?
    ): List<MedicinalProductResponse> {
        val results = medicinalProductRepository.findByFilters(atcGroupId, substanceId, query)
        return results.map { MedicinalProductResponse(it.id!!, it.name) }
    }
}
