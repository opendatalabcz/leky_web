package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdAdministrationRoute
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdAdministrationRouteRepository : JpaRepository<MpdAdministrationRoute, Long> {
    fun findByCode(code: String): MpdAdministrationRoute?

    fun findAllByCodeIn(codes: Set<String>): List<MpdAdministrationRoute>
}
