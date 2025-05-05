package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdGovernmentRegulationCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdGovernmentRegulationCategoryRepository : JpaRepository<MpdGovernmentRegulationCategory, Long> {
    fun findByCode(code: String): MpdGovernmentRegulationCategory?

    fun findAllByCodeIn(codes: Set<String>): List<MpdGovernmentRegulationCategory>
}
