package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDopingCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdDopingCategoryRepository : JpaRepository<MpdDopingCategory, String> {

    fun findAllByCodeIn(codes: Set<String>): List<MpdDopingCategory>
}