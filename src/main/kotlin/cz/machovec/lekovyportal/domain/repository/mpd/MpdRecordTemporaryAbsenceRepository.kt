package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdRecordTemporaryAbsence
import org.springframework.data.jpa.repository.JpaRepository

interface MpdRecordTemporaryAbsenceRepository : JpaRepository<MpdRecordTemporaryAbsence, Long>