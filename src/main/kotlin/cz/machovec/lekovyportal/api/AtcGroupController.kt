package cz.machovec.lekovyportal.api

import cz.machovec.lekovyportal.api.dto.AtcGroupDto
import cz.machovec.lekovyportal.service.AtcGroupService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/atc-groups")
class AtcGroupController(
    private val atcGroupService: AtcGroupService
) {

    @GetMapping
    fun getAll(): List<AtcGroupDto> = atcGroupService.getAll()
}
