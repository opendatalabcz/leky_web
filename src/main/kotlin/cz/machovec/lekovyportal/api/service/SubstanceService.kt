package cz.machovec.lekovyportal.api.service

import cz.machovec.lekovyportal.api.model.SubstanceResponse
import cz.machovec.lekovyportal.core.repository.mpd.MpdSubstanceRepository
import org.springframework.stereotype.Service

@Service
class SubstanceService(
    private val repository: MpdSubstanceRepository
) {
    fun searchSubstances(query: String): List<SubstanceResponse> {
        return repository.findTop30ByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(query, query)
            .map { SubstanceResponse(it.id!!, it.name!!, it.code) }
    }
}
