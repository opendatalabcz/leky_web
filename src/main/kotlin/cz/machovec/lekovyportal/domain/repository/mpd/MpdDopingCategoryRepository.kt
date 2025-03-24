package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDopingCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdDopingCategoryRepository : JpaRepository<MpdDopingCategory, Long> {
    fun findByCode(code: String): MpdDopingCategory?

    fun findAllByCodeIn(codes: Set<String>): List<MpdDopingCategory>
}