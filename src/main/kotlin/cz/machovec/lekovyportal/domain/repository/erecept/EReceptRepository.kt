package cz.machovec.lekovyportal.domain.repository.erecept

import java.time.YearMonth

interface EReceptRepository {
    fun findAggregatesByDistrict(
        medicinalProductIds: List<Long>,
        dateFrom: YearMonth? = null,
        dateTo: YearMonth? = null
    ): List<EReceptDistrictDataRow>

    fun findRawMonthlyAggregates(
        medicinalProductIds: List<Long>
    ): List<EReceptRawMonthlyAggregate>
}
