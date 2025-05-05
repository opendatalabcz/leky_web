package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdAddictionCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdAddictionCategoryRepository : JpaRepository<MpdAddictionCategory, Long> {
    fun findByCode(code: String): MpdAddictionCategory?

    fun findAllByCodeIn(codes: Set<String>): List<MpdAddictionCategory>
}
