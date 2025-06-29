package cz.machovec.lekovyportal.api.calculations.distribution

import SankeyLinkDto
import SankeyNodeDto
import cz.machovec.lekovyportal.api.calculations.DoseUnitConverter
import cz.machovec.lekovyportal.core.domain.distribution.*
import cz.machovec.lekovyportal.core.dto.distribution.AggregateDistributorProductMovementCountDto
import cz.machovec.lekovyportal.core.dto.distribution.AggregateMahProductMovementCountDto
import cz.machovec.lekovyportal.core.dto.distribution.AggregatePharmacyProductDispenseCountDto
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class SankeyDiagramAssembler(
    private val labelResolver: NodeLabelResolver
) {

    fun assemble(
        mah: List<AggregateMahProductMovementCountDto>,
        dist: List<AggregateDistributorProductMovementCountDto>,
        pharm: List<AggregatePharmacyProductDispenseCountDto>,
        converter: DoseUnitConverter,
        dddPerProduct: Map<Long, BigDecimal>
    ): Pair<List<SankeyNodeDto>, List<SankeyLinkDto>> {

        val nodes = mutableSetOf<SankeyNodeDto>()
        val links = mutableListOf<SankeyLinkDto>()

        fun addLink(sourceId: String, sourceLabel: String, targetId: String, targetLabel: String, value: Long) {
            if (value <= 0) return
            nodes += SankeyNodeDto(sourceId, sourceLabel)
            nodes += SankeyNodeDto(targetId,  targetLabel)
            links += SankeyLinkDto(sourceId, targetId, value.toInt())
        }

        /* ---------- MAH  ---------- */

        mah.filter { it.purchaserType == MahPurchaserType.DISTRIBUTOR }
            .groupBy { it.movementType }
            .let { g ->
                val delivered = g[MovementType.DELIVERY]?.sumOf {
                    converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
                } ?: BigDecimal.ZERO
                val returned  = g[MovementType.RETURN]?.sumOf {
                    converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
                } ?: BigDecimal.ZERO
                addLink(
                    "MAH", "Registrátor",
                    "Distributor", "Distributor",
                    (delivered - returned).toLong()
                )
            }

        mah.filter { it.purchaserType == MahPurchaserType.AUTHORIZED_PERSON }
            .groupBy { it.movementType }
            .let { g ->
                val delivered = g[MovementType.DELIVERY]?.sumOf {
                    converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
                } ?: BigDecimal.ZERO
                val returned  = g[MovementType.RETURN]?.sumOf {
                    converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
                } ?: BigDecimal.ZERO
                val targetId    = labelResolver.nodeIdForMahPurchaser(MahPurchaserType.AUTHORIZED_PERSON)
                val targetLabel = labelResolver.nodeLabelForMahPurchaser(MahPurchaserType.AUTHORIZED_PERSON)
                addLink("MAH", "Registrátor", targetId, targetLabel, (delivered - returned).toLong())
            }


        dist.groupBy { it.purchaserType }.forEach { (purchType, list) ->
            val delivered = list.filter { it.movementType == MovementType.DELIVERY }.sumOf {
                converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
            }
            val returned  = list.filter { it.movementType == MovementType.RETURN }.sumOf {
                converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
            }
            val net = delivered - returned
            if (net <= BigDecimal.ZERO || purchType == DistributorPurchaserType.DISTRIBUTOR_CR) return@forEach

            val targetId    = labelResolver.nodeIdForDistributorPurchaser(purchType)
            val targetLabel = labelResolver.nodeLabelForDistributorPurchaser(purchType)
            addLink("Distributor", "Distributor", targetId, targetLabel, net.toLong())
        }


        pharm.groupBy { it.dispenseType }.forEach { (dispType, list) ->
            val total = list.sumOf {
                converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
            }
            if (total > BigDecimal.ZERO) {
                val id    = labelResolver.nodeIdForDispenseType(dispType)
                val label = labelResolver.nodeLabelForDispenseType(dispType)
                addLink("Pharmacy", "Lékárna", id, label, total.toLong())
            }
        }

        return nodes.toList() to links
    }
}
