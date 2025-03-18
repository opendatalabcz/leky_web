package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdRegistrationException
import org.springframework.data.jpa.repository.JpaRepository

interface MpdRegistrationExceptionRepository : JpaRepository<MpdRegistrationException, Long> {
}