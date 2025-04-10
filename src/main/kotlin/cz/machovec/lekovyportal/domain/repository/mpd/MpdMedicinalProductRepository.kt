package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProduct
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdMedicinalProductRepository : JpaRepository<MpdMedicinalProduct, Long>, MpdMedicinalProductRepositoryCustom {
    fun findBySuklCode(suklCode: String): MpdMedicinalProduct?

    fun findAllByIdIn(ids: List<Long>): List<MpdMedicinalProduct>

    fun findAllBySuklCodeIn(suklCodes: Set<String>): List<MpdMedicinalProduct>
}
