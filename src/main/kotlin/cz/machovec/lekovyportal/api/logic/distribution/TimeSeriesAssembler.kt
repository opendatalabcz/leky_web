// logic/distribution/TimeSeriesAssembler.kt
package cz.machovec.lekovyportal.api.logic.distribution

import DistributionFlowEntry
import DistributionTimeSeriesPeriodEntry
import cz.machovec.lekovyportal.api.logic.DoseUnitConverter
import cz.machovec.lekovyportal.api.model.enums.TimeGranularity
import cz.machovec.lekovyportal.core.domain.distribution.*
import cz.machovec.lekovyportal.core.dto.distribution.MonthlyDistributorProductMovementCountDto
import cz.machovec.lekovyportal.core.dto.distribution.MonthlyMahProductMovementCountDto
import cz.machovec.lekovyportal.core.dto.distribution.MonthlyPharmacyProductDispenseCountDto
import org.springframework.stereotype.Component

@Component
class TimeSeriesAssembler(
    private val labelResolver: NodeLabelResolver
) {

    fun assemble(
        mah: List<MonthlyMahProductMovementCountDto>,
        dist: List<MonthlyDistributorProductMovementCountDto>,
        pharm: List<MonthlyPharmacyProductDispenseCountDto>,
        granularity: TimeGranularity,
        converter: DoseUnitConverter,
        dddPerProduct: Map<Long, java.math.BigDecimal>
    ): List<DistributionTimeSeriesPeriodEntry> {

        fun periodKey(y: Int, m: Int): String =
            if (granularity == TimeGranularity.YEAR) y.toString() else "%04d-%02d".format(y, m)

        /* všechny period keys, aby i „prázdné“ měsíce měly záznam */
        val allPeriods = buildSet {
            mah.forEach { add(periodKey(it.year, it.month)) }
            dist.forEach { add(periodKey(it.year, it.month)) }
            pharm.forEach { add(periodKey(it.year, it.month)) }
        }.sorted()

        return allPeriods.map { key ->
            val flows = mutableListOf<DistributionFlowEntry>()

            /* ---------- MAH ---------- */
            mah.filter { periodKey(it.year, it.month) == key && it.purchaserType == MahPurchaserType.DISTRIBUTOR }
                .groupBy { it.movementType }
                .let { g ->
                    val deliv = g[MovementType.DELIVERY]?.sumOf {
                        converter.convert(it.medicinalProductId, it.packageCount.toLong(), dddPerProduct)
                    } ?: 0L
                    val ret   = g[MovementType.RETURN]?.sumOf {
                        converter.convert(it.medicinalProductId, it.packageCount.toLong(), dddPerProduct)
                    } ?: 0L
                    val net   = deliv - ret
                    if (net > 0) flows += DistributionFlowEntry("MAH", "Distributor", net.toInt())
                }

            mah.filter { periodKey(it.year, it.month) == key && it.purchaserType == MahPurchaserType.AUTHORIZED_PERSON }
                .groupBy { it.movementType }
                .let { g ->
                    val deliv = g[MovementType.DELIVERY]?.sumOf {
                        converter.convert(it.medicinalProductId, it.packageCount.toLong(), dddPerProduct)
                    } ?: 0L
                    val ret   = g[MovementType.RETURN]?.sumOf {
                        converter.convert(it.medicinalProductId, it.packageCount.toLong(), dddPerProduct)
                    } ?: 0L
                    val net   = deliv - ret
                    if (net > 0) flows += DistributionFlowEntry("MAH", "OOV", net.toInt())
                }

            /* ---------- Distributor ---------- */
            dist.filter { periodKey(it.year, it.month) == key }
                .groupBy { it.purchaserType }
                .forEach { (purch, list) ->
                    val deliv = list.filter { it.movementType == MovementType.DELIVERY }.sumOf {
                        converter.convert(it.medicinalProductId, it.packageCount.toLong(), dddPerProduct)
                    }
                    val ret   = list.filter { it.movementType == MovementType.RETURN }.sumOf {
                        converter.convert(it.medicinalProductId, it.packageCount.toLong(), dddPerProduct)
                    }
                    val net = deliv - ret
                    if (net <= 0) return@forEach

                    val targetId = labelResolver.nodeIdForDistributorPurchaser(purch)
                    flows += DistributionFlowEntry("Distributor", targetId, net.toInt())
                }

            /* ---------- Pharmacy ---------- */
            pharm.filter { periodKey(it.year, it.month) == key }
                .groupBy { it.dispenseType }
                .forEach { (disp, list) ->
                    val total = list.sumOf {
                        converter.convert(it.medicinalProductId, it.packageCount.toLong(), dddPerProduct)
                    }
                    if (total > 0) {
                        val target = labelResolver.nodeIdForDispenseType(disp)
                        flows += DistributionFlowEntry("Pharmacy", target, total.toInt())
                    }
                }

            DistributionTimeSeriesPeriodEntry(period = key, flows = flows)
        }
    }
}
