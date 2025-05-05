package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.api.model.MpdMedicinalProductGroupedByRegNumberDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface MpdMedicinalProductGroupedByRegNumberRepository {
    fun findByFilters(
        atcGroupId: Long?,
        substanceId: Long?,
        query: String?,
        pageable: Pageable
    ): Page<MpdMedicinalProductGroupedByRegNumberDto>

    fun findByRegistrationNumbers(regNumbers: List<String>): List<MpdMedicinalProductGroupedByRegNumberDto>
}
