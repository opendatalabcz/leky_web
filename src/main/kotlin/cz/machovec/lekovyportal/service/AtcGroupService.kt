package cz.machovec.lekovyportal.service

import cz.machovec.lekovyportal.api.dto.AtcGroupDto
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAtcGroupRepository
import org.springframework.stereotype.Service

@Service
class AtcGroupService(
    private val atcGroupRepository: MpdAtcGroupRepository
) {
    fun getAll(): List<AtcGroupDto> {
        return atcGroupRepository.findAllByOrderByNameAsc()
            .map { AtcGroupDto(it.id!!, it.code, it.name) }
    }
}
