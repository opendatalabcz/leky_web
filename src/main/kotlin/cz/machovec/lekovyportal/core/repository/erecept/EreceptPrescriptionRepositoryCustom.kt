package cz.machovec.lekovyportal.core.repository.erecept

import cz.machovec.lekovyportal.core.domain.erecept.EreceptPrescription

interface EreceptPrescriptionRepositoryCustom {
    fun batchInsert(records: List<EreceptPrescription>, batchSize: Int = 1000)
}