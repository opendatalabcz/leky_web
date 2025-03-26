package cz.machovec.lekovyportal.domain.repository

import cz.machovec.lekovyportal.domain.entity.EreceptDispense

interface EreceptDispenseRepositoryCustom {
    fun batchInsert(records: List<EreceptDispense>, batchSize: Int = 1000)
}