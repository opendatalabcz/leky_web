package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProductSubstance
import org.springframework.data.jpa.repository.JpaRepository

interface MpdMedicinalProductSubstanceRepository : JpaRepository<MpdMedicinalProductSubstance, Long>