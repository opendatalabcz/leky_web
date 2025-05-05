package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdRecordTemporaryAbsence
import org.springframework.data.jpa.repository.JpaRepository

interface MpdRecordTemporaryAbsenceRepository : JpaRepository<MpdRecordTemporaryAbsence, Long>