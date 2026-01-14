package cz.machovec.lekovyportal.core.repository.erecept

import cz.machovec.lekovyportal.core.dto.erecept.EreceptAggregatedDistrictDto
import cz.machovec.lekovyportal.core.dto.erecept.EreceptFullTimeSeriesDto
import cz.machovec.lekovyportal.core.dto.erecept.EreceptTimeSeriesDistrictDto
import java.time.YearMonth

interface EreceptRepository {

    fun getAggregatedByDistrictRows(
        medicinalProductIds: List<Long>,
        dateFrom: YearMonth? = null,
        dateTo: YearMonth? = null
    ): List<EreceptAggregatedDistrictDto>

    fun getTimeSeriesByDistrictRows(
        medicinalProductIds: List<Long>,
        dateFrom: YearMonth,
        dateTo: YearMonth
    ): List<EreceptTimeSeriesDistrictDto>

    fun getFullTimeSeriesRows(
        medicinalProductIds: List<Long>,
        districtCode: String?
    ): List<EreceptFullTimeSeriesDto>
}
