package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdSubstance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdSubstanceRepository : JpaRepository<MpdSubstance, Long> {

    fun findAllByCodeIn(codes: Set<String>): List<MpdSubstance>
}
