package cz.machovec.lekovyportal.domain.repository

import cz.machovec.lekovyportal.domain.entity.EreceptPrescription

interface EreceptPrescriptionRepositoryCustom {
    fun batchInsert(records: List<EreceptPrescription>, batchSize: Int = 1000)
}