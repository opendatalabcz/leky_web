package cz.machovec.lekovyportal.api.calculations

import SummaryValues
import cz.machovec.lekovyportal.api.model.enums.MedicinalUnitMode
import cz.machovec.lekovyportal.core.dto.erecept.EreceptAggregatedDistrictDto
import cz.machovec.lekovyportal.core.dto.erecept.EreceptTimeSeriesDistrictDto
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class SummaryCalculator(
    private val converterFactory: DoseUnitConverterFactory
) {

    fun fromDistrictRows(
        rows: List<EreceptAggregatedDistrictDto>,
        unitMode: MedicinalUnitMode,
        dddPerProduct: Map<Long, BigDecimal>
    ): SummaryValues {
        val converter = converterFactory.of(unitMode)

        return build(
            rows.sumOf { converter.convert(it.medicinalProductId, it.prescribed, dddPerProduct) },
            rows.sumOf { converter.convert(it.medicinalProductId, it.dispensed, dddPerProduct) }
        )
    }

    fun fromMonthlyRows(
        rows: List<EreceptTimeSeriesDistrictDto>,
        unitMode: MedicinalUnitMode,
        dddPerProduct: Map<Long, BigDecimal>
    ): SummaryValues {
        val converter = converterFactory.of(unitMode)

        return build(
            rows.sumOf { converter.convert(it.medicinalProductId, it.prescribed, dddPerProduct) },
            rows.sumOf { converter.convert(it.medicinalProductId, it.dispensed, dddPerProduct) }
        )
    }

    private fun build(prescribed: BigDecimal, dispensed: BigDecimal): SummaryValues {
        val diff = prescribed - dispensed

        val prescribedInt = prescribed.setScale(0, RoundingMode.HALF_UP).toInt()
        val dispensedInt  = dispensed.setScale(0, RoundingMode.HALF_UP).toInt()
        val diffInt       = diff.setScale(0, RoundingMode.HALF_UP).toInt()

        val pct = if (prescribed.compareTo(BigDecimal.ZERO) == 0) {
            0.0
        } else {
            diff.divide(prescribed, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
                .setScale(1, RoundingMode.HALF_UP)
                .toDouble()
        }

        return SummaryValues(prescribedInt, dispensedInt, diffInt, pct)
    }
}
