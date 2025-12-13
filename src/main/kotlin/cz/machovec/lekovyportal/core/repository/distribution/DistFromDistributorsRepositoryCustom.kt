package cz.machovec.lekovyportal.core.repository.distribution

import cz.machovec.lekovyportal.core.domain.distribution.DistFromDistributors

interface DistFromDistributorsRepositoryCustom {
    fun batchInsert(records: List<DistFromDistributors>, batchSize: Int = 1000)
}
