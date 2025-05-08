package cz.machovec.lekovyportal.api.logic

import SummaryValues
import cz.machovec.lekovyportal.core.repository.erecept.EReceptDistrictDataRow
import cz.machovec.lekovyportal.core.repository.erecept.EReceptMonthlyDistrictAggregate
import org.springframework.stereotype.Component

@Component
class SummaryCalculator {

    fun fromDistrictRows(rows: List<EReceptDistrictDataRow>): SummaryValues =
        build(rows.sumOf { it.prescribed }, rows.sumOf { it.dispensed })

    fun fromMonthlyRows(rows: List<EReceptMonthlyDistrictAggregate>): SummaryValues =
        build(rows.sumOf { it.prescribed }, rows.sumOf { it.dispensed })

    private fun build(prescribed: Int, dispensed: Int): SummaryValues {
        val diff   = prescribed - dispensed
        val pct    = if (prescribed == 0) 0.0 else diff.toDouble() / prescribed * 100
        return SummaryValues(prescribed, dispensed, diff, pct)
    }
}
