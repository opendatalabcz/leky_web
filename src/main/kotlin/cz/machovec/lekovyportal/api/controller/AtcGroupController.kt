package cz.machovec.lekovyportal.api.controller

import cz.machovec.lekovyportal.api.model.mpd.AtcGroupResponse
import cz.machovec.lekovyportal.api.service.AtcGroupService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/atc-groups")
class AtcGroupController(
    private val atcGroupService: AtcGroupService
) {
    @GetMapping
    fun search(
        @RequestParam(required = false) query: String?
    ): List<AtcGroupResponse> {
        return if (query.isNullOrBlank()) {
            emptyList()
        } else {
            atcGroupService.search(query)
        }
    }
}
