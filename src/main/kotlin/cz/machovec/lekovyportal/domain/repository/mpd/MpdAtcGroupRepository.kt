package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdAtcGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdAtcGroupRepository : JpaRepository<MpdAtcGroup, Long> {
    fun findByCode(code: String): MpdAtcGroup?

    fun findAllByCodeIn(codes: Set<String>): List<MpdAtcGroup>
}
