package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdAttributeChange
import org.springframework.data.jpa.repository.JpaRepository

interface MpdAttributeChangeRepository : JpaRepository<MpdAttributeChange, Long>
