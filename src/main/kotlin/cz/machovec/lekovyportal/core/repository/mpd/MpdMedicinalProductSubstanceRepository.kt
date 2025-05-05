package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdMedicinalProductSubstance
import org.springframework.data.jpa.repository.JpaRepository

interface MpdMedicinalProductSubstanceRepository : JpaRepository<MpdMedicinalProductSubstance, Long>