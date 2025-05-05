package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdPackageType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdPackageTypeRepository : JpaRepository<MpdPackageType, Long> {
    fun findByCode(code: String): MpdPackageType?

    fun findAllByCodeIn(codes: Set<String>): List<MpdPackageType>
}
