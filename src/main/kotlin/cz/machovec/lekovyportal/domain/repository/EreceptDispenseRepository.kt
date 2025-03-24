package cz.machovec.lekovyportal.domain.repository

import cz.machovec.lekovyportal.domain.entity.EreceptDispense
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EreceptDispenseRepository : JpaRepository<EreceptDispense, Long>, EreceptDispenseRepositoryCustom {
    fun findByMedicinalProductIdAndYearAndMonth(
        medicinalProductId: Long,
        year: Int,
        month: Int
    ): List<EreceptDispense>
}
