package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProductComposition
import org.springframework.data.jpa.repository.JpaRepository

interface MpdMedicinalProductCompositionRepository : JpaRepository<MpdMedicinalProductComposition, Long>