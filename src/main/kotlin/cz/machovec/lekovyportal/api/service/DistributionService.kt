package cz.machovec.lekovyportal.api.service

import DistributionSankeyRequest
import DistributionSankeyResponse
import DistributionTimeSeriesEntry
import DistributionTimeSeriesRequest
import DistributionTimeSeriesResponse
import SankeyLinkDto
import SankeyNodeDto
import cz.machovec.lekovyportal.api.model.enums.TimeGranularity
import cz.machovec.lekovyportal.api.model.mpd.MedicinalProductIdentificators
import cz.machovec.lekovyportal.core.domain.distribution.DistributorPurchaserType
import cz.machovec.lekovyportal.core.domain.distribution.MahPurchaserType
import cz.machovec.lekovyportal.core.domain.distribution.MovementType
import cz.machovec.lekovyportal.core.domain.distribution.PharmacyDispenseType
import cz.machovec.lekovyportal.core.repository.distribution.DistFromDistributorsRepository
import cz.machovec.lekovyportal.core.repository.distribution.DistFromMahsRepository
import cz.machovec.lekovyportal.core.repository.distribution.DistFromPharmaciesRepository
import cz.machovec.lekovyportal.core.repository.mpd.MpdMedicinalProductRepository
import org.springframework.stereotype.Service

@Service
class DistributionService(
    private val mahRepo: DistFromMahsRepository,
    private val distRepo: DistFromDistributorsRepository,
    private val pharmRepo: DistFromPharmaciesRepository,
    private val medicinalProductRepo: MpdMedicinalProductRepository
) {

    fun getSankeyDiagram(req: DistributionSankeyRequest): DistributionSankeyResponse {
        val productsById = if (req.medicinalProductIds.isNotEmpty()) {
            medicinalProductRepo.findAllByIdIn(req.medicinalProductIds)
        } else emptyList()

        val productsByRegNumbers = if (req.registrationNumbers.isNotEmpty()) {
            medicinalProductRepo.findAllByRegistrationNumberIn(req.registrationNumbers)
        } else emptyList()

        val allProducts = (productsById + productsByRegNumbers).distinctBy { it.id }

        val (included, ignored) = allProducts.partition { it.id != null }
        val productIds = included.mapNotNull { it.id }

        val (fromYear, fromMonth) = req.dateFrom.split("-").let { it[0].toInt() to it[1].toInt() }
        val (toYear, toMonth) = req.dateTo.split("-").let { it[0].toInt() to it[1].toInt() }

        val mahAggList = mahRepo.sumByPurchaser(productIds, fromYear, fromMonth, toYear, toMonth)
        val distAggList = distRepo.sumByPurchaser(productIds, fromYear, fromMonth, toYear, toMonth)
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

        val mahToOov = net(mahMap[MahPurchaserType.AUTHORIZED_PERSON] ?: (0 to 0))
        if (mahToOov > 0) {
            addNode("OOV", "Osoba oprávněná k výdeji (Lékař, Lékárna, ...)")
            addLink("MAH", "OOV", mahToOov)
        }

        purchaserLabels.forEach { (type, label) ->
            if (type == DistributorPurchaserType.PHARMACY || type == DistributorPurchaserType.DISTRIBUTOR_CR) return@forEach

            val value = net(distMap[type] ?: (0 to 0))
            if (value > 0) {
                addNode(type.name, label)
                addLink("Distributor", type.name, value)
            }
        }

        val pharmacyValue = net(distMap[DistributorPurchaserType.PHARMACY] ?: (0 to 0))
        if (pharmacyValue > 0) {
            addLink("Distributor", "Pharmacy", pharmacyValue)
        }

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
            includedMedicineProducts = included.map { MedicinalProductIdentificators(it.id!!, it.suklCode) },
            ignoredMedicineProducts = ignored.map { MedicinalProductIdentificators(it.id!!, it.suklCode) },
            dateFrom = req.dateFrom,
            dateTo = req.dateTo,
            calculationMode = req.calculationMode,
            nodes = nodes,
            links = links
        )
    }

    fun getTimeSeries(request: DistributionTimeSeriesRequest): DistributionTimeSeriesResponse {
        val productsById = if (request.medicinalProductIds.isNotEmpty()) {
            medicinalProductRepo.findAllByIdIn(request.medicinalProductIds)
        } else emptyList()

        val productsByRegNumbers = if (request.registrationNumbers.isNotEmpty()) {
            medicinalProductRepo.findAllByRegistrationNumberIn(request.registrationNumbers)
        } else emptyList()

        val allProducts = (productsById + productsByRegNumbers).distinctBy { it.id }

        val (included, ignored) = allProducts.partition { it.id != null }
        val productIds = included.mapNotNull { it.id }

        val mahRaw = mahRepo.findMonthlyAggregates(productIds)
        val distRaw = distRepo.findMonthlyAggregates(productIds)
        val pharmRaw = pharmRepo.findMonthlyAggregates(productIds)

        fun getPeriod(year: Int, month: Int): String =
            if (request.timeGranularity == TimeGranularity.YEAR) year.toString()
            else "%04d-%02d".format(year, month)

        val mahGrouped = mahRaw
            .filter { it.purchaserType == MahPurchaserType.DISTRIBUTOR }
            .groupBy { getPeriod(it.year, it.month) }

        val mahFlow = mahGrouped.mapValues { (_, list) ->
            val del = list.filter { it.movementType == MovementType.DELIVERY }.sumOf { it.totalCount }
            val ret = list.filter { it.movementType == MovementType.RETURN }.sumOf { it.totalCount }
            del - ret
        }

        val distGrouped = distRaw
            .filter { it.purchaserType == DistributorPurchaserType.PHARMACY }
            .groupBy { getPeriod(it.year, it.month) }

        val distFlow = distGrouped.mapValues { (_, list) ->
            val del = list.filter { it.movementType == MovementType.DELIVERY }.sumOf { it.totalCount }
            val ret = list.filter { it.movementType == MovementType.RETURN }.sumOf { it.totalCount }
            del - ret
        }

        val pharmGrouped = pharmRaw.groupBy { getPeriod(it.year, it.month) }
        val pharmFlow = pharmGrouped.mapValues { (_, list) ->
            list.sumOf { it.packageCount }.toInt()
        }

        val allPeriods = (mahFlow.keys + distFlow.keys + pharmFlow.keys).toSortedSet()

        val series = allPeriods.map { period ->
            DistributionTimeSeriesEntry(
                period = period,
                mahToDistributor = (mahFlow[period] ?: 0L).toInt(),
                distributorToPharmacy = (distFlow[period] ?: 0L).toInt(),
                pharmacyToPatient = (pharmFlow[period] ?: 0).toInt()
            )
        }

        return DistributionTimeSeriesResponse(
            includedMedicineProducts = included.map { MedicinalProductIdentificators(it.id!!, it.suklCode) },
            ignoredMedicineProducts = ignored.map { MedicinalProductIdentificators(it.id!!, it.suklCode) },
            dateFrom = request.dateFrom,
            dateTo = request.dateTo,
            calculationMode = request.calculationMode,
            timeGranularity = request.timeGranularity,
            series = series
        )
    }
}
