package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdCancelledRegistration
import org.springframework.data.jpa.repository.JpaRepository

interface MpdCancelledRegistrationRepository : JpaRepository<MpdCancelledRegistration, Long>
