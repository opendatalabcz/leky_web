package cz.machovec.lekovyportal.core.repository.distribution

import cz.machovec.lekovyportal.core.domain.distribution.DistExportFromDistributors

interface DistExportFromDistributorsRepositoryCustom {
    fun batchInsert(records: List<DistExportFromDistributors>, batchSize: Int = 1000)
}
