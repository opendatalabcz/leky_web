package cz.machovec.lekovyportal.api.logic

import cz.machovec.lekovyportal.api.model.enums.EreceptType
import cz.machovec.lekovyportal.api.model.enums.MedicinalUnitMode
import cz.machovec.lekovyportal.api.model.enums.NormalisationMode
import cz.machovec.lekovyportal.core.repository.erecept.EReceptDistrictDataRow
import cz.machovec.lekovyportal.core.repository.erecept.EReceptMonthlyDistrictAggregate
import kotlin.math.roundToInt

/**
 * All heavy-weight arithmetic for eRecept endpoints in one place.
 * No Spring annotations – plain Kotlin → easy unit-testing.
 */
class DistrictAggregator(
    private val converter: DoseUnitConverter
) {

    /* ---------- helpers ---------- */

    private fun pickValue(
        type: EreceptType,
        prescribed: Long,
        dispensed: Long
    ) = when (type) {
        EreceptType.PRESCRIBED -> prescribed
        EreceptType.DISPENSED  -> dispensed
        EreceptType.DIFFERENCE -> prescribed - dispensed
    }

    private fun normalisePerThousand(
        raw: Long,
        population: Int,
        normMode: NormalisationMode
    ): Long =
        if (normMode == NormalisationMode.ABSOLUTE || population <= 0) raw
        else (raw / (population / 1000.0)).roundToInt().toLong()

    /* ---------- public API ---------- */

    /**
     * Aggregates district-level rows (all months collapsed).
     */
    fun aggregateDistrictRows(
        rows: List<EReceptDistrictDataRow>,
        aggType: EreceptType,
        unitMode: MedicinalUnitMode,
        normMode: NormalisationMode
    ): Map<String, Int> =
        rows
            .groupBy { it.districtCode }
            .mapValues { (_, districtRows) ->
                var prescribed = 0L
                var dispensed  = 0L
                var population = 0

                districtRows.forEach { r ->
                    prescribed += converter.convert(r.medicinalProductId, r.prescribed.toLong(), unitMode)
                    dispensed  += converter.convert(r.medicinalProductId, r.dispensed .toLong(), unitMode)
                    population  = r.population
                }

                val raw   = pickValue(aggType, prescribed, dispensed)
                val final = normalisePerThousand(raw, population, normMode)

                final.toInt()
            }

    /**
     * Aggregates monthly+distric rows (one map per month).
     */
    fun aggregateMonthlyDistrictRows(
        rows: List<EReceptMonthlyDistrictAggregate>,
        aggType: EreceptType,
        unitMode: MedicinalUnitMode,
        normMode: NormalisationMode
    ): Map<String, Int> =
        rows
            .groupBy { it.districtCode }
            .mapValues { (_, districtRows) ->
                var prescribed = 0L
                var dispensed  = 0L
                var population = 0

                districtRows.forEach { r ->
                    prescribed += converter.convert(r.medicinalProductId, r.prescribed.toLong(), unitMode)
                    dispensed  += converter.convert(r.medicinalProductId, r.dispensed .toLong(), unitMode)
                    population  = r.population
                }

                val raw   = pickValue(aggType, prescribed, dispensed)
                val final = normalisePerThousand(raw, population, normMode)

                final.toInt()
            }
}
