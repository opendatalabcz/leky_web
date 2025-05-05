package cz.machovec.lekovyportal.core.repository.erecept

import cz.machovec.lekovyportal.core.domain.erecept.EreceptPrescription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EreceptPrescriptionRepository : JpaRepository<EreceptPrescription, Long>,
    EreceptPrescriptionRepositoryCustom {
    fun findByMedicinalProductIdAndYearAndMonth(
        medicinalProductId: Long,
        year: Int,
        month: Int
    ): List<EreceptPrescription>
}