package cz.machovec.lekovyportal.api.service

import cz.machovec.lekovyportal.api.model.mpd.AtcGroupResponse
import cz.machovec.lekovyportal.core.repository.mpd.MpdAtcGroupRepository
import org.springframework.stereotype.Service

@Service
class AtcGroupService(
    private val atcGroupRepository: MpdAtcGroupRepository
) {
    fun search(query: String): List<AtcGroupResponse> {
        return if (query.length == 1) {
            atcGroupRepository.findByCodeIgnoreCase(query)
                ?.let { listOf(AtcGroupResponse(it.id!!, it.code, it.name)) }
                ?: emptyList()
        } else {
            atcGroupRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrderByNameAsc(query, query)
                .map { AtcGroupResponse(it.id!!, it.code, it.name) }
        }
    }
}
