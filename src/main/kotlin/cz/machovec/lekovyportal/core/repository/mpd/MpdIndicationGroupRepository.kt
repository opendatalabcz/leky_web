package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdIndicationGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdIndicationGroupRepository : JpaRepository<MpdIndicationGroup, Long> {
    fun findByCode(code: String): MpdIndicationGroup?

    fun findAllByCodeIn(codes: Set<String>): List<MpdIndicationGroup>
}
