package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProduct
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface MpdMedicinalProductRepositoryCustom {
    fun findByFilters(
        atcGroupId: Long?,
        substanceId: Long?,
        query: String?,
        pageable: Pageable
    ): Page<MpdMedicinalProduct>
}
