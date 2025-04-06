package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.api.dto.MpdMedicinalProductGroupedByRegNumberDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface MpdMedicinalProductGroupedByRegNumberRepository {
    fun findByFilters(
        atcGroupId: Long?,
        substanceId: Long?,
        query: String?,
        pageable: Pageable
    ): Page<MpdMedicinalProductGroupedByRegNumberDto>
}
