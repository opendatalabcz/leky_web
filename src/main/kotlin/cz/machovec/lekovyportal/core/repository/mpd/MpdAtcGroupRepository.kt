package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdAtcGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdAtcGroupRepository : JpaRepository<MpdAtcGroup, Long> {
    fun findByCode(code: String): MpdAtcGroup?

    fun findAllByCodeIn(codes: Set<String>): List<MpdAtcGroup>

    fun findAllByOrderByNameAsc(): List<MpdAtcGroup>
}
