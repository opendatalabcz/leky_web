package cz.machovec.lekovyportal.api.service

import cz.machovec.lekovyportal.api.model.DistributionSankeyRequest
import cz.machovec.lekovyportal.api.model.DistributionSankeyResponse
import cz.machovec.lekovyportal.api.model.MedicinalProductIdentificators
import cz.machovec.lekovyportal.api.model.SankeyLinkDto
import cz.machovec.lekovyportal.api.model.SankeyNodeDto
import cz.machovec.lekovyportal.core.domain.distribution.DistributorPurchaserType
import cz.machovec.lekovyportal.core.domain.distribution.MahPurchaserType
import cz.machovec.lekovyportal.core.domain.distribution.MovementType
import cz.machovec.lekovyportal.core.domain.distribution.PharmacyDispenseType
import cz.machovec.lekovyportal.core.repository.distribution.DistFromDistributorsRepository
import cz.machovec.lekovyportal.core.repository.distribution.DistFromMahsRepository
import cz.machovec.lekovyportal.core.repository.distribution.DistFromPharmaciesRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdMedicinalProductRepository
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
            includedMedicineProducts = included.map { MedicinalProductIdentificators(it.id!!, it.suklCode) },
            ignoredMedicineProducts = ignored.map { MedicinalProductIdentificators(it.id!!, it.suklCode) }
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

        DistributorPurchaserType.entries
            .filter { it != DistributorPurchaserType.DISTRIBUTOR_CR }
            .forEach { type ->
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
            includedMedicineProducts = included.map { MedicinalProductIdentificators(it.id!!, it.suklCode) },
            ignoredMedicineProducts = ignored.map { MedicinalProductIdentificators(it.id!!, it.suklCode) }
        )
    }

    fun buildFullDistributionSankey(req: DistributionSankeyRequest): DistributionSankeyResponse {
        val allProducts = mpRepo.findAllByIdIn(req.medicinalProductIds)
        val (included, ignored) = allProducts.partition { it.id != null }
        val productIds = included.mapNotNull { it.id }

        val (fromYear, fromMonth) = req.dateFrom.split("-").let { it[0].toInt() to it[1].toInt() }
        val (toYear, toMonth) = req.dateTo.split("-").let { it[0].toInt() to it[1].toInt() }

        val mahAggList = mahRepo.sumByPurchaser(productIds, fromYear, fromMonth, toYear, toMonth)
        val distAggList = distrRepo.sumByPurchaser(productIds, fromYear, fromMonth, toYear, toMonth)
        val pharmacyAggList = pharmRepo.sumPackagesByDispenseType(productIds, fromYear, fromMonth, toYear, toMonth)

        fun net(pair: Pair<Int, Int>) = pair.first - pair.second

        val purchaserLabels = mapOf(
            DistributorPurchaserType.DISTRIBUTOR_EU to "Distributor (EU)",
            DistributorPurchaserType.DISTRIBUTOR_NON_EU to "Distributor (mimo EU)",
            DistributorPurchaserType.DOCTOR to "Lékař",
            DistributorPurchaserType.PHARMACY to "Lékárna",
            DistributorPurchaserType.NUCLEAR_MEDICINE to "Nukleární medicína",
            DistributorPurchaserType.SALES_REPRESENTATIVE to "Reklamní vzorky",
            DistributorPurchaserType.HEALTHCARE_PROVIDER to "Zdravotnické zařízení",
            DistributorPurchaserType.VLP_SELLER to "Prodejce VLP",
            DistributorPurchaserType.TRANSFUSION_SERVICE to "Transfuzní služba",
            DistributorPurchaserType.VETERINARY_DOCTOR to "Veterinární lékař",
            DistributorPurchaserType.FOREIGN_ENTITY to "Zahraniční subjekt"
        )

        val mahMap = mahAggList.groupBy { it.purchaserType }.mapValues { (_, list) ->
            val del = list.filter { it.movementType == MovementType.DELIVERY }.sumOf { it.totalCount }
            val ret = list.filter { it.movementType == MovementType.RETURN }.sumOf { it.totalCount }
            del to ret
        }

        val distMap = distAggList.groupBy { it.purchaserType }.mapValues { (_, list) ->
            val del = list.filter { it.movementType == MovementType.DELIVERY }.sumOf { it.totalCount }
            val ret = list.filter { it.movementType == MovementType.RETURN }.sumOf { it.totalCount }
            del to ret
        }

        val nodes = mutableListOf(
            SankeyNodeDto("MAH", "Registrátor"),
            SankeyNodeDto("Distributor", "Distributor"),
            SankeyNodeDto("Pharmacy", "Lékárna")
        )
        val links = mutableListOf<SankeyLinkDto>()

        fun addNode(id: String, label: String) {
            if (nodes.none { it.id == id }) nodes += SankeyNodeDto(id, label)
        }

        fun addLink(src: String, tgt: String, value: Int) {
            if (value > 0) links += SankeyLinkDto(src, tgt, value)
        }

        // MAH to Distributor
        addLink("MAH", "Distributor", net(mahMap[MahPurchaserType.DISTRIBUTOR] ?: (0 to 0)))

        // OOV typy
        val oovTypes = listOf(
            DistributorPurchaserType.DOCTOR,
            DistributorPurchaserType.PHARMACY,
            DistributorPurchaserType.NUCLEAR_MEDICINE,
            DistributorPurchaserType.SALES_REPRESENTATIVE,
            DistributorPurchaserType.HEALTHCARE_PROVIDER,
            DistributorPurchaserType.VLP_SELLER,
            DistributorPurchaserType.TRANSFUSION_SERVICE,
            DistributorPurchaserType.VETERINARY_DOCTOR
        )

        // Vytvořit název uzlu OOV s výpisem
        val oovLabels = oovTypes.mapNotNull { purchaserLabels[it] }.sorted()
        val oovNodeLabel = "OOV (${oovLabels.joinToString(", ")})"

        // MAH to OOV (shrnutý uzel)
        val mahToOov = net(mahMap[MahPurchaserType.AUTHORIZED_PERSON] ?: (0 to 0))
        if (mahToOov > 0) {
            addNode("OOV", "Osoba oprávněná k výdeji (Lékař, Lékárna, ...)")
            addLink("MAH", "OOV", mahToOov)
        }

        // Distributor flows (excluding PHARMACY a DISTRIBUTOR_CR)
        purchaserLabels.forEach { (type, label) ->
            if (type == DistributorPurchaserType.PHARMACY || type == DistributorPurchaserType.DISTRIBUTOR_CR) return@forEach

            val value = net(distMap[type] ?: (0 to 0))
            if (value > 0) {
                addNode(type.name, label)
                addLink("Distributor", type.name, value)
            }
        }

        // Distributor to Pharmacy
        val pharmacyValue = net(distMap[DistributorPurchaserType.PHARMACY] ?: (0 to 0))
        if (pharmacyValue > 0) {
            addLink("Distributor", "Pharmacy", pharmacyValue)
        }

        // Pharmacy to dispense types
        pharmacyAggList.forEach { row ->
            val dispenseType = row.dispenseType
            val total = row.total

            val nodeId = when (dispenseType) {
                PharmacyDispenseType.PRESCRIPTION -> "Prescription"
                PharmacyDispenseType.REQUISITION -> "Requisition"
                PharmacyDispenseType.OTC -> "OTC"
            }

            val label = when (dispenseType) {
                PharmacyDispenseType.PRESCRIPTION -> "Výdej na předpis"
                PharmacyDispenseType.REQUISITION -> "Výdej na žádanku"
                PharmacyDispenseType.OTC -> "Volný prodej"
            }

            addNode(nodeId, label)
            addLink("Pharmacy", nodeId, total.toInt())
        }

        return DistributionSankeyResponse(
            nodes = nodes,
            links = links,
            includedMedicineProducts = included.map { MedicinalProductIdentificators(it.id!!, it.suklCode) },
            ignoredMedicineProducts = ignored.map { MedicinalProductIdentificators(it.id!!, it.suklCode) }
        )
    }
}
