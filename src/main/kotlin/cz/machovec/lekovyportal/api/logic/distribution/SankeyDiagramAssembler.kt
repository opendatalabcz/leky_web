package cz.machovec.lekovyportal.api.logic.distribution

import SankeyLinkDto
import SankeyNodeDto
import cz.machovec.lekovyportal.api.logic.DoseUnitConverter
import cz.machovec.lekovyportal.core.domain.distribution.*
import cz.machovec.lekovyportal.core.dto.distribution.AggregateDistributorProductMovementCountDto
import cz.machovec.lekovyportal.core.dto.distribution.AggregateMahProductMovementCountDto
import cz.machovec.lekovyportal.core.dto.distribution.AggregatePharmacyProductDispenseCountDto
import org.springframework.stereotype.Component

@Component
class SankeyDiagramAssembler(
    private val labelResolver: NodeLabelResolver
) {

    fun assemble(
        mah: List<AggregateMahProductMovementCountDto>,
        dist: List<AggregateDistributorProductMovementCountDto>,
        pharm: List<AggregatePharmacyProductDispenseCountDto>,
        converter: DoseUnitConverter,
        dddPerProduct: Map<Long, java.math.BigDecimal>
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
                } ?: 0L
                val returned  = g[MovementType.RETURN]?.sumOf {
                    converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
                } ?: 0L
                addLink(
                    "MAH", "Registrátor",
                    "Distributor", "Distributor",
                    delivered - returned
                )
            }

        mah.filter { it.purchaserType == MahPurchaserType.AUTHORIZED_PERSON }
            .groupBy { it.movementType }
            .let { g ->
                val delivered = g[MovementType.DELIVERY]?.sumOf {
                    converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
                } ?: 0L
                val returned  = g[MovementType.RETURN]?.sumOf {
                    converter.convert(it.medicinalProductId, it.packageCount.toLong(), dddPerProduct)
                } ?: 0L
                val targetId    = labelResolver.nodeIdForMahPurchaser(MahPurchaserType.DISTRIBUTOR)
                val targetLabel = labelResolver.nodeLabelForMahPurchaser(MahPurchaserType.DISTRIBUTOR)
                addLink("MAH", "Registrátor", targetId, targetLabel, delivered - returned)
            }


        dist.groupBy { it.purchaserType }.forEach { (purchType, list) ->
            val delivered = list.filter { it.movementType == MovementType.DELIVERY }.sumOf {
                converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
            }
            val returned  = list.filter { it.movementType == MovementType.RETURN }.sumOf {
                converter.convert(it.medicinalProductId, it.packageCount, dddPerProduct)
            }
            val net = delivered - returned
            if (net <= 0 || purchType == DistributorPurchaserType.DISTRIBUTOR_CR) return@forEach

            val targetId    = labelResolver.nodeIdForDistributorPurchaser(purchType)
            val targetLabel = labelResolver.nodeLabelForDistributorPurchaser(purchType)
            addLink("Distributor", "Distributor", targetId, targetLabel, net)
        }


        pharm.groupBy { it.dispenseType }.forEach { (dispType, list) ->
            val total = list.sumOf {
                converter.convert(it.medicinalProductId, it.packageCount.toLong(), dddPerProduct)
            }
            if (total > 0) {
                val id    = labelResolver.nodeIdForDispenseType(dispType)
                val label = labelResolver.nodeLabelForDispenseType(dispType)
                addLink("Pharmacy", "Lékárna", id, label, total)
            }
        }

        return nodes.toList() to links
    }
}
