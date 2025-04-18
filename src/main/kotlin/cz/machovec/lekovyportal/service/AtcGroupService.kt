package cz.machovec.lekovyportal.service

import cz.machovec.lekovyportal.api.dto.AtcGroupResponse
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAtcGroupRepository
import org.springframework.stereotype.Service

@Service
class AtcGroupService(
    private val atcGroupRepository: MpdAtcGroupRepository
) {
    fun getAll(): List<AtcGroupResponse> {
        return atcGroupRepository.findAllByOrderByNameAsc()
            .map { AtcGroupResponse(it.id!!, it.code, it.name) }
    }
}
