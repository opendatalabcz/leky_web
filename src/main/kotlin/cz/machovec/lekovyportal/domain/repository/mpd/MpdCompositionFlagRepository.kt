package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdCompositionFlag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdCompositionFlagRepository : JpaRepository<MpdCompositionFlag, Long> {

    fun findAllByCodeIn(codes: Set<String>): List<MpdCompositionFlag>
}
