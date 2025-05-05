package cz.machovec.lekovyportal.api.service

import cz.machovec.lekovyportal.api.model.AtcGroupResponse
import cz.machovec.lekovyportal.core.repository.mpd.MpdAtcGroupRepository
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
