package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdAddictionCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdAddictionCategoryRepository : JpaRepository<MpdAddictionCategory, Long> {
    fun findAllByCodeIn(codes: Set<String>): List<MpdAddictionCategory>
}
