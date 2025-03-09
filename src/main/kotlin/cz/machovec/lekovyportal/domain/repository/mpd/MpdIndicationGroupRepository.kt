package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdIndicationGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdIndicationGroupRepository : JpaRepository<MpdIndicationGroup, String> {

    fun findAllByCodeIn(codes: Set<String>): List<MpdIndicationGroup>
}
