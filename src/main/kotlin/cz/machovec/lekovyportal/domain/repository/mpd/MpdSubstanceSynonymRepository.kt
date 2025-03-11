package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdSource
import cz.machovec.lekovyportal.domain.entity.mpd.MpdSubstance
import cz.machovec.lekovyportal.domain.entity.mpd.MpdSubstanceSynonym
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdSubstanceSynonymRepository : JpaRepository<MpdSubstanceSynonym, Long> {
    fun findAllBySubstanceAndSource(substance: MpdSubstance, source: MpdSource): List<MpdSubstanceSynonym>
}
