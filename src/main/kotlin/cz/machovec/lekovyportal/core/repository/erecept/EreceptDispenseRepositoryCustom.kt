package cz.machovec.lekovyportal.core.repository.erecept

import cz.machovec.lekovyportal.core.domain.erecept.EreceptDispense

interface EreceptDispenseRepositoryCustom {
    fun batchInsert(records: List<EreceptDispense>, batchSize: Int = 1000)
}