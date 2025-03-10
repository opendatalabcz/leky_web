package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdCountry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdCountryRepository : JpaRepository<MpdCountry, Long> {

    fun findAllByCodeIn(codes: Set<String>): List<MpdCountry>
}
