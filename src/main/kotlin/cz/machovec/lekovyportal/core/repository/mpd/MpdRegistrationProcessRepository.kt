package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdRegistrationProcess
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdRegistrationProcessRepository : JpaRepository<MpdRegistrationProcess, Long> {
    fun findByCode(code: String): MpdRegistrationProcess?

    fun findAllByCodeIn(codes: Set<String>): List<MpdRegistrationProcess>
}
