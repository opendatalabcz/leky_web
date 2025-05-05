package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdCountry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdCountryRepository : JpaRepository<MpdCountry, Long> {
    fun findByCode(code: String): MpdCountry?

    fun findAllByCodeIn(codes: Set<String>): List<MpdCountry>
}
