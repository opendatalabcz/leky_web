package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdCancelledRegistration
import org.springframework.data.jpa.repository.JpaRepository

interface MpdCancelledRegistrationRepository : JpaRepository<MpdCancelledRegistration, Long>
