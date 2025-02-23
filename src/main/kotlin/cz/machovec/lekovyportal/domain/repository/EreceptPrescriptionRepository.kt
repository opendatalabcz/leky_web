package cz.machovec.lekovyportal.domain.repository

import cz.machovec.lekovyportal.domain.entity.EreceptPrescription
import cz.machovec.lekovyportal.domain.entity.EreceptPrescriptionId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EreceptPrescriptionRepository : JpaRepository<EreceptPrescription, EreceptPrescriptionId>