package cz.machovec.lekovyportal.service

import cz.machovec.lekovyportal.api.dto.DistributionSankeyRequest
import cz.machovec.lekovyportal.api.dto.DistributionSankeyResponse
import cz.machovec.lekovyportal.api.dto.MedicineProductInfo
import cz.machovec.lekovyportal.api.dto.SankeyLinkDto
import cz.machovec.lekovyportal.api.dto.SankeyNodeDto
import cz.machovec.lekovyportal.domain.entity.distribution.DistributorPurchaserType
import cz.machovec.lekovyportal.domain.entity.distribution.MahPurchaserType
import cz.machovec.lekovyportal.domain.entity.distribution.MovementType
import cz.machovec.lekovyportal.domain.repository.dist.DistFromDistributorsRepository
import cz.machovec.lekovyportal.domain.repository.dist.DistFromMahsRepository
import cz.machovec.lekovyportal.domain.repository.dist.DistFromPharmaciesRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMedicinalProductRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class DistributionService(
    private val mahRepo: DistFromMahsRepository,
    private val distrRepo: DistFromDistributorsRepository,
    private val mpRepo: MpdMedicinalProductRepository,
    private val pharmRepo: DistFromPharmaciesRepository,
) {

    fun buildMahFlowSankey(req: DistributionSankeyRequest): DistributionSankeyResponse {
        val allProducts = mpRepo.findAllByIdIn(req.medicinalProductIds)
        val (included, ignored) = allProducts.partition { it.id != null }

        val fromYear = req.dateFrom.substringBefore("-").toInt()
        val fromMonth = req.dateFrom.substringAfter("-").toInt()
        val toYear = req.dateTo.substringBefore("-").toInt()
        val toMonth = req.dateTo.substringAfter("-").toInt()

        val mahAggList = mahRepo.sumByPurchaser(
            included.mapNotNull { it.id },
            fromYear, fromMonth, toYear, toMonth
        )

        val distAggList = distrRepo.sumByPurchaser(
            included.mapNotNull { it.id },
            fromYear, fromMonth, toYear, toMonth
        )

        fun net(pair: Pair<Int, Int>) = pair.first - pair.second

        val mahMap: Map<MahPurchaserType, Pair<Int, Int>> =
            mahAggList
                .groupBy { it.purchaserType }
                .mapValues { (_, list) ->
                    val del = list.filter { it.movementType == MovementType.DELIVERY }.sumOf { it.totalCount }
                    val ret = list.filter { it.movementType == MovementType.RETURN }.sumOf { it.totalCount }
                    del to ret
                }

        val distMap: Map<DistributorPurchaserType, Pair<Int, Int>> =
            distAggList
                .groupBy { it.purchaserType }
                .mapValues { (_, list) ->
                    val del = list.filter { it.movementType == MovementType.DELIVERY }.sumOf { it.totalCount }
                    val ret = list.filter { it.movementType == MovementType.RETURN }.sumOf { it.totalCount }
                    del to ret
                }

        val mahToDistributor = net(mahMap[MahPurchaserType.DISTRIBUTOR] ?: (0 to 0))
        val mahToOov = net(mahMap[MahPurchaserType.AUTHORIZED_PERSON] ?: (0 to 0))

        val distributorToForeign = net(
            distMap.filterKeys {
                it in listOf(
                    DistributorPurchaserType.DISTRIBUTOR_EU,
                    DistributorPurchaserType.DISTRIBUTOR_NON_EU,
                    DistributorPurchaserType.FOREIGN_ENTITY
                )
            }.values.fold(0 to 0) { a, b -> a.first + b.first to a.second + b.second }
        )

        val distributorToOov = net(
            distMap.filterKeys {
                it in listOf(
                    DistributorPurchaserType.DOCTOR,
                    DistributorPurchaserType.PHARMACY,
                    DistributorPurchaserType.NUCLEAR_MEDICINE,
                    DistributorPurchaserType.HEALTHCARE_PROVIDER,
                    DistributorPurchaserType.TRANSFUSION_SERVICE
                )
            }.values.fold(0 to 0) { a, b -> a.first + b.first to a.second + b.second }
        )

        val distributorToOthers = net(
            distMap.filterKeys {
                it in listOf(
                    DistributorPurchaserType.SALES_REPRESENTATIVE,
                    DistributorPurchaserType.VLP_SELLER,
                    DistributorPurchaserType.VETERINARY_DOCTOR
                )
            }.values.fold(0 to 0) { a, b -> a.first + b.first to a.second + b.second }
        )

        val nodes = mutableListOf(
            SankeyNodeDto("MAH", "Registrátor"),
            SankeyNodeDto("Distributor", "Distributor")
        )

        val links = mutableListOf<SankeyLinkDto>()

        fun addNode(id: String, label: String) {
            if (nodes.none { it.id == id }) nodes += SankeyNodeDto(id, label)
        }

        fun addLink(src: String, tgt: String, value: Int) {
            if (value > 0) links += SankeyLinkDto(src, tgt, value)
        }

        addLink("MAH", "Distributor", mahToDistributor)

        if (mahToOov > 0 || distributorToOov > 0) {
            addNode("OOV", "OOV (souhrn)")
            addLink("MAH", "OOV", mahToOov)
            addLink("Distributor", "OOV", distributorToOov)
        }

        if (distributorToForeign > 0) {
            addNode("Foreign", "Zahraniční subjekty")
            addLink("Distributor", "Foreign", distributorToForeign)
        }

        if (distributorToOthers > 0) {
            addNode("Others", "Ostatní odběratelé")
            addLink("Distributor", "Others", distributorToOthers)
        }

        return DistributionSankeyResponse(
            nodes = nodes,
            links = links,
            includedMedicineProducts = included.map { MedicineProductInfo(it.id!!, it.suklCode) },
            ignoredMedicineProducts = ignored.map { MedicineProductInfo(it.id!!, it.suklCode) }
        )
    }

    fun buildDistributorFlowSankey(req: DistributionSankeyRequest): DistributionSankeyResponse {
        val allProducts = mpRepo.findAllByIdIn(req.medicinalProductIds)
        val (included, ignored) = allProducts.partition { it.id != null }
        val productIds = included.mapNotNull { it.id }

        fun parseYearMonth(date: String): Pair<Int, Int> {
            val parts = date.split("-")
            return parts[0].toInt() to parts[1].toInt()
        }

        val (fromYear, fromMonth) = parseYearMonth(req.dateFrom)
        val (toYear, toMonth) = parseYearMonth(req.dateTo)

        val distAggList = distrRepo.sumByPurchaser(
            productIds, fromYear, fromMonth, toYear, toMonth
        )

        val pharmacyTotal = pharmRepo.sumPackages(
            productIds, fromYear, fromMonth, toYear, toMonth
        )

        fun net(pair: Pair<Int, Int>) = pair.first - pair.second

        val distMap: Map<DistributorPurchaserType, Pair<Int, Int>> =
            distAggList
                .groupBy { it.purchaserType }
                .mapValues { (_, list) ->
                    val del = list.filter { it.movementType == MovementType.DELIVERY }.sumOf { it.totalCount }
                    val ret = list.filter { it.movementType == MovementType.RETURN }.sumOf { it.totalCount }
                    del to ret
                }

        val nodes = mutableListOf(SankeyNodeDto("Distributor", "Distributor"))
        val links = mutableListOf<SankeyLinkDto>()

        fun addNode(id: String, label: String) {
            if (nodes.none { it.id == id }) nodes += SankeyNodeDto(id, label)
        }

        fun addLink(src: String, tgt: String, value: Int) {
            if (value > 0) links += SankeyLinkDto(src, tgt, value)
        }

        DistributorPurchaserType.entries.forEach { type ->
            val value = net(distMap[type] ?: (0 to 0))
            if (value > 0) {
                addNode(type.name, type.descriptionCs)
                addLink("Distributor", type.name, value)
            }
        }

        if (pharmacyTotal != null && pharmacyTotal > BigDecimal.ZERO) {
            addNode("Patients", "Pacienti")
            addLink(DistributorPurchaserType.PHARMACY.name, "Patients", pharmacyTotal.toInt())
        }

        return DistributionSankeyResponse(
            nodes = nodes,
            links = links,
            includedMedicineProducts = included.map { MedicineProductInfo(it.id!!, it.suklCode) },
            ignoredMedicineProducts = ignored.map { MedicineProductInfo(it.id!!, it.suklCode) }
        )
    }
}
