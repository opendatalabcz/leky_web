package cz.machovec.lekovyportal.domain.repository.erecept

import org.springframework.stereotype.Repository

@Repository
interface EreceptRepository {
    fun findAggregatesByDistrict(productIds: List<Long>): List<DistrictAggregateRow>
}
