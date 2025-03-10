package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdGovernmentRegulationCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdGovernmentRegulationCategoryRepository : JpaRepository<MpdGovernmentRegulationCategory, Long> {

    fun findAllByCodeIn(codes: Set<String>): List<MpdGovernmentRegulationCategory>
}
