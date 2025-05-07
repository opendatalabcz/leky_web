package cz.machovec.lekovyportal.core.repository.erecept

import java.time.YearMonth

interface EreceptRepository {
    fun findAggregatesAllDistricts(
        medicinalProductIds: List<Long>,
        dateFrom: YearMonth? = null,
        dateTo: YearMonth? = null
    ): List<EReceptDistrictDataRow>

    fun findMonthlyAllDistricts(
        medicinalProductIds: List<Long>,
        dateFrom: YearMonth,
        dateTo: YearMonth
    ): List<EReceptMonthlyDistrictAggregate>

    fun findFullMonthly(
        medicinalProductIds: List<Long>
    ): List<EReceptRawMonthlyAggregate>
}
