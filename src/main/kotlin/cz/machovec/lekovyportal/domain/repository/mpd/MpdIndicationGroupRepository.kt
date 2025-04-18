package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdIndicationGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdIndicationGroupRepository : JpaRepository<MpdIndicationGroup, Long> {
    fun findByCode(code: String): MpdIndicationGroup?

    fun findAllByCodeIn(codes: Set<String>): List<MpdIndicationGroup>
}
