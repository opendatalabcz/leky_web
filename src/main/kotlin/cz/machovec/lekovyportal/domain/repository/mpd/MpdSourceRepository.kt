package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdSource
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdSourceRepository : JpaRepository<MpdSource, Long> {

    fun findAllByCodeIn(codes: Set<String>): List<MpdSource>
}
