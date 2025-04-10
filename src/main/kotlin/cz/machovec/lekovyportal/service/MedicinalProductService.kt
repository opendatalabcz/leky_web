package cz.machovec.lekovyportal.service

import cz.machovec.lekovyportal.api.dto.AdministrationRouteResponse
import cz.machovec.lekovyportal.api.dto.AtcGroupResponse
import cz.machovec.lekovyportal.api.dto.DosageFormResponse
import cz.machovec.lekovyportal.api.dto.MedicinalProductGroupedByRegNumberResponse
import cz.machovec.lekovyportal.api.dto.MedicinalProductResponse
import cz.machovec.lekovyportal.api.dto.PagedResponse
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAdministrationRouteRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAtcGroupRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDosageFormRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMedicinalProductGroupedByRegNumberRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMedicinalProductRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class MedicinalProductService(
    private val medicinalProductRepository: MpdMedicinalProductRepository,
    private val groupedViewRepository: MpdMedicinalProductGroupedByRegNumberRepository,
    private val dosageFormRepository: MpdDosageFormRepository,
    private val administrationRouteRepository: MpdAdministrationRouteRepository,
    private val atcGroupRepository: MpdAtcGroupRepository
) {
    fun searchMedicinalProducts(
        atcGroupId: Long?,
        substanceId: Long?,
        query: String?,
        page: Int,
        size: Int
    ): PagedResponse<MedicinalProductResponse> {
        val pageable = PageRequest.of(page, size)
        val resultPage = medicinalProductRepository.findByFilters(atcGroupId, substanceId, query, pageable)

        val content = resultPage.content.map { product ->
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

        return PagedResponse(
            content = content,
            totalElements = resultPage.totalElements,
            totalPages = resultPage.totalPages,
            currentPage = resultPage.number
        )
    }

    fun searchMedicinalProductsGroupedByRegNumber(
        atcGroupId: Long?,
        substanceId: Long?,
        query: String?,
        page: Int,
        size: Int
    ): PagedResponse<MedicinalProductGroupedByRegNumberResponse> {
        val pageable = PageRequest.of(page, size)
        val resultPage = groupedViewRepository.findByFilters(atcGroupId, substanceId, query, pageable)

        val dosageForms = dosageFormRepository.findAllById(
            resultPage.content.flatMap { it.dosageFormIds }.toSet()
        ).associateBy { it.id }

        val adminRoutes = administrationRouteRepository.findAllById(
            resultPage.content.flatMap { it.administrationRouteIds }.toSet()
        ).associateBy { it.id }

        val atcGroups = atcGroupRepository.findAllById(
            resultPage.content.flatMap { it.atcGroupIds }.toSet()
        ).associateBy { it.id }

        val content = resultPage.content.map { view ->
            MedicinalProductGroupedByRegNumberResponse(
                registrationNumber = view.registrationNumber,
                suklCodes = view.suklCodes,
                names = view.names,
                strengths = view.strengths,
                dosageForms = view.dosageFormIds.mapNotNull { id ->
                    dosageForms[id]?.let { DosageFormResponse(it.id!!, it.code, it.name) }
                },
                administrationRoutes = view.administrationRouteIds.mapNotNull { id ->
                    adminRoutes[id]?.let { AdministrationRouteResponse(it.id!!, it.code, it.name) }
                },
                atcGroups = view.atcGroupIds.mapNotNull { id ->
                    atcGroups[id]?.let { AtcGroupResponse(it.id!!, it.code, it.name) }
                }
            )
        }

        return PagedResponse(
            content = content,
            totalElements = resultPage.totalElements,
            totalPages = resultPage.totalPages,
            currentPage = resultPage.number
        )
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
