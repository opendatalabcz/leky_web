package cz.machovec.lekovyportal.api.logic

import cz.machovec.lekovyportal.api.model.enums.EreceptType
import cz.machovec.lekovyportal.api.model.enums.MedicinalUnitMode
import cz.machovec.lekovyportal.api.model.enums.NormalisationMode
import cz.machovec.lekovyportal.core.repository.erecept.EReceptDistrictDataRow
import cz.machovec.lekovyportal.core.repository.erecept.EReceptMonthlyDistrictAggregate
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DistrictAggregator(
    private val converterFactory: DoseUnitConverterFactory,
    private val normaliserFactory: PopulationNormaliserFactory
) {

    fun aggregate(
        rows: List<EReceptDistrictDataRow>,
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
                    converter.convert(it.medicinalProductId, it.prescribed.toLong(), dddPerProduct)
                }
                val dispensed  = districtRows.sumOf {
                    converter.convert(it.medicinalProductId, it.dispensed.toLong(), dddPerProduct)
                }
                val population = districtRows.first().population

                val raw = when (aggType) {
                    EreceptType.PRESCRIBED -> prescribed
                    EreceptType.DISPENSED  -> dispensed
                    EreceptType.DIFFERENCE -> prescribed - dispensed
                }

                normaliser.normalise(raw, population).toInt()
            }
    }

    fun aggregateMonthly(
        rows: List<EReceptMonthlyDistrictAggregate>,
        aggType: EreceptType,
        unitMode: MedicinalUnitMode,
        normMode: NormalisationMode,
        dddPerProduct: Map<Long, BigDecimal>
    ): Map<String, Int> = aggregate(
        rows.map {
            EReceptDistrictDataRow(
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
        rawPackages: Long,
        unitMode: MedicinalUnitMode,
        dddPerProduct: Map<Long, BigDecimal>
    ): Long {
        val converter = converterFactory.of(unitMode)
        return converter.convert(productId, rawPackages, dddPerProduct)
    }
}
