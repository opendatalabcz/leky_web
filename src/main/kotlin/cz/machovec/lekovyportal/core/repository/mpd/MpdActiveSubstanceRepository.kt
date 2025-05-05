package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdActiveSubstance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdActiveSubstanceRepository : JpaRepository<MpdActiveSubstance, Long> {
    fun findAllByCodeIn(codes: Set<String>): List<MpdActiveSubstance>
}
