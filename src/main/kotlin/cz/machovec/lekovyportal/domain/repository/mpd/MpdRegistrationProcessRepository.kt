package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdRegistrationProcess
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdRegistrationProcessRepository : JpaRepository<MpdRegistrationProcess, Long> {

    fun findAllByCodeIn(codes: Set<String>): List<MpdRegistrationProcess>
}
