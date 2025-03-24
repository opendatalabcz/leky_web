package cz.machovec.lekovyportal.domain.repository

import cz.machovec.lekovyportal.domain.entity.EreceptPrescription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EreceptPrescriptionRepository : JpaRepository<EreceptPrescription, Long>, EreceptPrescriptionRepositoryCustom {
    fun findByMedicinalProductIdAndYearAndMonth(
        medicinalProductId: Long,
        year: Int,
        month: Int
    ): List<EreceptPrescription>
}