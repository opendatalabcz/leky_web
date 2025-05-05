package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdSubstance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdSubstanceRepository : JpaRepository<MpdSubstance, Long> {
    fun findByCode(code: String): MpdSubstance?

    fun findAllByCodeIn(codes: Set<String>): List<MpdSubstance>

    fun findTop30ByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(name: String, code: String): List<MpdSubstance>
}
