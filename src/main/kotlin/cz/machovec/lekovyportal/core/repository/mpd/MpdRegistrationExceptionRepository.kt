package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdRegistrationException
import org.springframework.data.jpa.repository.JpaRepository

interface MpdRegistrationExceptionRepository : JpaRepository<MpdRegistrationException, Long> {
}