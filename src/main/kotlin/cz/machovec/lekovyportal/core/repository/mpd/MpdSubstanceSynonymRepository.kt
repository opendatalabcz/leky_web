package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdSource
import cz.machovec.lekovyportal.core.domain.mpd.MpdSubstance
import cz.machovec.lekovyportal.core.domain.mpd.MpdSubstanceSynonym
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdSubstanceSynonymRepository : JpaRepository<MpdSubstanceSynonym, Long> {
    fun findAllBySubstanceAndSource(substance: MpdSubstance, source: MpdSource): List<MpdSubstanceSynonym>
}
