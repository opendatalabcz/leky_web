package cz.machovec.lekovyportal.api.calculations

import cz.machovec.lekovyportal.api.model.enums.EreceptType
import cz.machovec.lekovyportal.api.model.enums.MedicinalUnitMode
import cz.machovec.lekovyportal.api.model.enums.NormalisationMode
import cz.machovec.lekovyportal.core.dto.erecept.EreceptAggregatedDistrictDto
import cz.machovec.lekovyportal.core.dto.erecept.EreceptTimeSeriesDistrictDto
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class DistrictAggregator(
    private val converterFactory: DoseUnitConverterFactory,
    private val normaliserFactory: PopulationNormaliserFactory
) {

    fun aggregate(
        rows: List<EreceptAggregatedDistrictDto>,
        aggType: EreceptType,
        unitMode: MedicinalUnitMode,
        normMode: NormalisationMode,
        dddPerProduct: Map<Long, BigDecimal>
    ): Map<String, Int> {

        val converter   = converterFactory.of(unitMode)
        val normaliser  = normaliserFactory.of(normMode)

        return rows.groupBy { it.districtCode }
            .mapValues { (_, districtRows) ->

                val prescribed = districtRows.sumOf {
                    converter.convert(it.medicinalProductId, it.prescribed, dddPerProduct)
                }
                val dispensed  = districtRows.sumOf {
                    converter.convert(it.medicinalProductId, it.dispensed, dddPerProduct)
                }
                val population = districtRows.first().population

                val raw = when (aggType) {
                    EreceptType.PRESCRIBED -> prescribed
                    EreceptType.DISPENSED  -> dispensed
                    EreceptType.DIFFERENCE -> prescribed - dispensed
                }

                normaliser.normalise(raw, population)
                    .setScale(0, RoundingMode.HALF_UP)
                    .toInt()
            }
    }

    fun aggregateMonthly(
        rows: List<EreceptTimeSeriesDistrictDto>,
        aggType: EreceptType,
        unitMode: MedicinalUnitMode,
        normMode: NormalisationMode,
        dddPerProduct: Map<Long, BigDecimal>
    ): Map<String, Int> = aggregate(
        rows.map {
            EreceptAggregatedDistrictDto(
                districtCode       = it.districtCode,
                medicinalProductId = it.medicinalProductId,
                prescribed         = it.prescribed,
                dispensed          = it.dispensed,
                population         = it.population,
            )
        },
        aggType, unitMode, normMode, dddPerProduct
    )

    fun convertValue(
        productId: Long,
        rawPackages: BigDecimal,
        unitMode: MedicinalUnitMode,
        dddPerProduct: Map<Long, BigDecimal>
    ): BigDecimal {
        val converter = converterFactory.of(unitMode)
        return converter.convert(productId, rawPackages, dddPerProduct)
    }
}
