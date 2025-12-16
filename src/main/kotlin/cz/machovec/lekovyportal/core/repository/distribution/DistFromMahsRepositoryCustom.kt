package cz.machovec.lekovyportal.core.repository.distribution

import cz.machovec.lekovyportal.core.domain.distribution.DistFromMahs

interface DistFromMahsRepositoryCustom {
    fun batchInsert(records: List<DistFromMahs>, batchSize: Int = 1000)
}
