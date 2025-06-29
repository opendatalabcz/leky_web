package cz.machovec.lekovyportal.api.calculations.distribution

import DistributionFlowEntry
import DistributionTimeSeriesPeriodEntry
import cz.machovec.lekovyportal.api.calculations.DoseUnitConverter
import cz.machovec.lekovyportal.api.model.enums.TimeGranularity
import cz.machovec.lekovyportal.core.domain.distribution.*
import cz.machovec.lekovyportal.core.dto.distribution.MonthlyDistributorProductMovementCountDto
import cz.machovec.lekovyportal.core.dto.distribution.MonthlyMahProductMovementCountDto
import cz.machovec.lekovyportal.core.dto.distribution.MonthlyPharmacyProductDispenseCountDto
import org.springframework.stereotype.Component
import java.math.BigDecimal

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
        dddPerProduct: Map<Long, BigDecimal>
    ): List<DistributionTimeSeriesPeriodEntry> {

        fun periodKey(y: Int, m: Int): String =
            if (granularity == TimeGranularity.YEAR) y.toString() else "%04d-%02d".format(y, m)

        val allPeriods = buildSet {
            mah.forEach { add(periodKey(it.year, it.month)) }
            dist.forEach { add(periodKey(it.year, it.month)) }
            pharm.forEach { add(periodKey(it.year, it.month)) }
        }.sorted()

        return allPeriods.map { key ->
            val flows = mutableListOf<DistributionFlowEntry>()

            val sourceMahLabel = "Registrátor"
            val sourceDistributorLabel = "Distributor"
            val sourcePharmacyLabel = "Lékárna"

            // ---------- MAH → Distributor ----------
            mah.filter { periodKey(it.year, it.month) == key && it.purchaserType == MahPurchaserType.DISTRIBUTOR }
                .groupBy { it.movementType }
                .let { g ->
                    val deliv = g[MovementType.DELIVERY]?.sumOf {
                        converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
                    } ?: BigDecimal.ZERO
                    val ret = g[MovementType.RETURN]?.sumOf {
                        converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
                    } ?: BigDecimal.ZERO
                    val net = deliv - ret
                    if (net > BigDecimal.ZERO) flows += DistributionFlowEntry(sourceMahLabel, sourceDistributorLabel, net.toInt())
                }

            // ---------- MAH → OOV ----------
            mah.filter { periodKey(it.year, it.month) == key && it.purchaserType == MahPurchaserType.AUTHORIZED_PERSON }
                .groupBy { it.movementType }
                .let { g ->
                    val deliv = g[MovementType.DELIVERY]?.sumOf {
                        converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
                    } ?: BigDecimal.ZERO
                    val ret = g[MovementType.RETURN]?.sumOf {
                        converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
                    } ?: BigDecimal.ZERO
                    val net = deliv - ret
                    if (net > BigDecimal.ZERO) {
                        val target = labelResolver.nodeLabelForMahPurchaser(MahPurchaserType.AUTHORIZED_PERSON)
                        if (sourceMahLabel != target) flows += DistributionFlowEntry(sourceMahLabel, target, net.toInt())
                    }
                }

            // ---------- Distributor → Purchasers ----------
            dist.filter { periodKey(it.year, it.month) == key }
                .groupBy { it.purchaserType }
                .forEach { (purch, list) ->
                    val deliv = list.filter { it.movementType == MovementType.DELIVERY }.sumOf {
                        converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
                    }
                    val ret = list.filter { it.movementType == MovementType.RETURN }.sumOf {
                        converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
                    }
                    val net = deliv - ret
                    if (net <= BigDecimal.ZERO) return@forEach

                    val target = labelResolver.nodeLabelForDistributorPurchaser(purch)
                    if (sourceDistributorLabel != target) flows += DistributionFlowEntry(sourceDistributorLabel, target, net.toInt())
                }

            // ---------- Pharmacy → Dispense Types ----------
            pharm.filter { periodKey(it.year, it.month) == key }
                .groupBy { it.dispenseType }
                .forEach { (disp, list) ->
                    val total = list.sumOf {
                        converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
                    }
                    if (total > BigDecimal.ZERO) {
                        val target = labelResolver.nodeLabelForDispenseType(disp)
                        if (sourcePharmacyLabel != target) flows += DistributionFlowEntry(sourcePharmacyLabel, target, total.toInt())
                    }
                }

            DistributionTimeSeriesPeriodEntry(period = key, flows = flows)
        }
    }
}
