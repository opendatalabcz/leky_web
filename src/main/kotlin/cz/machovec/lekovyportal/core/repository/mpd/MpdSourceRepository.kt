package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdSource
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdSourceRepository : JpaRepository<MpdSource, Long> {
    fun findByCode(code: String): MpdSource?

    fun findAllByCodeIn(codes: Set<String>): List<MpdSource>
}
