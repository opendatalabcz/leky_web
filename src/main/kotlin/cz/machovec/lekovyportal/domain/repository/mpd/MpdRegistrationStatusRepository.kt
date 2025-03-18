package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdRegistrationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdRegistrationStatusRepository : JpaRepository<MpdRegistrationStatus, Long> {
    fun findByCode(code: String): MpdRegistrationStatus?

    fun findAllByCodeIn(codes: Set<String>): List<MpdRegistrationStatus>
}
