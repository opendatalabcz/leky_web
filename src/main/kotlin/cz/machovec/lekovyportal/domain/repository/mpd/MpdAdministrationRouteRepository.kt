package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdAdministrationRoute
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdAdministrationRouteRepository : JpaRepository<MpdAdministrationRoute, Long> {
    fun findAllByCodeIn(codes: Set<String>): List<MpdAdministrationRoute>
}
