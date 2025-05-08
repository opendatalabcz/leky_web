package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdMedicinalProduct
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdMedicinalProductRepository : JpaRepository<MpdMedicinalProduct, Long>,
    MpdMedicinalProductRepositoryCustom {
    fun findBySuklCode(suklCode: String): MpdMedicinalProduct?

    fun findAllByIdIn(ids: List<Long>): List<MpdMedicinalProduct>

    fun findAllBySuklCodeIn(suklCodes: Set<String>): List<MpdMedicinalProduct>

    fun findAllByRegistrationNumberIn(suklCodes: List<String>): List<MpdMedicinalProduct>
}
