package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdPackageType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdPackageTypeRepository : JpaRepository<MpdPackageType, Long> {
    fun findByCode(code: String): MpdPackageType?

    fun findAllByCodeIn(codes: Set<String>): List<MpdPackageType>
}
