package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdAttributeChange
import org.springframework.data.jpa.repository.JpaRepository

interface MpdAttributeChangeRepository : JpaRepository<MpdAttributeChange, Long>
