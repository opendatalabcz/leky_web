package cz.machovec.lekovyportal.service

import cz.machovec.lekovyportal.api.dto.AtcGroupResponse
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
        return results.map { product ->
            MedicinalProductResponse(
                id = product.id!!,
                name = product.name,
                supplementaryInformation = product.supplementaryInformation,
                suklCode = product.suklCode,
                registrationNumber = product.registrationNumber,
                atcGroup = product.atcGroup?.let {
                    AtcGroupResponse(
                        id = it.id!!,
                        name = it.name,
                        code = it.code
                    )
                }
            )
        }
    }

    fun findByIds(ids: List<Long>): List<MedicinalProductResponse> {
        return medicinalProductRepository.findAllById(ids).map { product ->
            MedicinalProductResponse(
                id = product.id!!,
                name = product.name,
                suklCode = product.suklCode,
                registrationNumber = product.registrationNumber,
                supplementaryInformation = product.supplementaryInformation,
                atcGroup = product.atcGroup?.let {
                    AtcGroupResponse(
                        id = it.id!!,
                        code = it.code,
                        name = it.name
                    )
                }
            )
        }
    }
}
