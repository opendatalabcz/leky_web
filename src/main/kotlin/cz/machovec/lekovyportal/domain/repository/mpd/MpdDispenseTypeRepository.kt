package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDispenseType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdDispenseTypeRepository : JpaRepository<MpdDispenseType, Long> {
    fun findByCode(code: String): MpdDispenseType?

    fun findAllByCodeIn(codes: Set<String>): List<MpdDispenseType>
}
