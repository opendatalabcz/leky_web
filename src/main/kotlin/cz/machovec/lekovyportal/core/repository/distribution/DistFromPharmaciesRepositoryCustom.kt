package cz.machovec.lekovyportal.core.repository.distribution

import cz.machovec.lekovyportal.core.domain.distribution.DistFromPharmacies

interface DistFromPharmaciesRepositoryCustom {
    fun batchInsert(records: List<DistFromPharmacies>, batchSize: Int = 1000)
}
