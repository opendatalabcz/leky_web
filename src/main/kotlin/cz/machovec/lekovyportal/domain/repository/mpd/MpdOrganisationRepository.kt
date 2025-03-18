package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdCountry
import cz.machovec.lekovyportal.domain.entity.mpd.MpdOrganisation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdOrganisationRepository : JpaRepository<MpdOrganisation, Long> {
    fun findByCodeAndCountryCode(code: String, countryCode: String): MpdOrganisation?

    fun findAllByCodeInAndCountry(codes: Set<String>, country: MpdCountry): List<MpdOrganisation>
}
