package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdMeasurementUnit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdMeasurementUnitRepository : JpaRepository<MpdMeasurementUnit, Long> {
    fun findByCode(code: String): MpdMeasurementUnit?

    fun findAllByCodeIn(codes: Set<String>): List<MpdMeasurementUnit>
}
