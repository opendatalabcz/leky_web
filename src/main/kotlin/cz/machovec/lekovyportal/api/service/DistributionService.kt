package cz.machovec.lekovyportal.api.service

import DistributionFlowEntry
import DistributionSankeyRequest
import DistributionSankeyResponse
import DistributionTimeSeriesPeriodEntry
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
import java.math.BigDecimal

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

        val mahAggregates = mahRepo.getAggregateProductMovementCounts(productIds, fromYear, fromMonth, toYear, toMonth)
        val distAggregates = distRepo.getAggregateDistributorProductMovementCounts(productIds, fromYear, fromMonth, toYear, toMonth)
        val pharmacyAggregates = pharmRepo.getAggregatePharmacyProductDispenseCounts(productIds, fromYear, fromMonth, toYear, toMonth)

        val nodes = mutableSetOf<SankeyNodeDto>()
        val links = mutableListOf<SankeyLinkDto>()

        fun addNode(id: String, label: String) {
            if (nodes.none { it.id == id }) nodes += SankeyNodeDto(id, label)
        }

        fun addLink(source: String, target: String, value: Long) {
            if (value > 0) links += SankeyLinkDto(source, target, value.toInt())
        }

        // Define labels
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

        // Add main nodes
        addNode("MAH", "Registrátor")
        addNode("Distributor", "Distributor")
        addNode("Pharmacy", "Lékárna")
        addNode("OOV", "Osoba oprávněná k výdeji (Lékař, Lékárna, ...)")

        // MAH → Distributor
        mahAggregates.filter { it.purchaserType == MahPurchaserType.DISTRIBUTOR }
            .groupBy { it.movementType }
            .let { grouped ->
                val delivered = grouped[MovementType.DELIVERY]?.sumOf { it.packageCount } ?: 0L
                val returned = grouped[MovementType.RETURN]?.sumOf { it.packageCount } ?: 0L
                val net = delivered - returned
                addLink("MAH", "Distributor", net)
            }

        // MAH → OOV
        mahAggregates.filter { it.purchaserType == MahPurchaserType.AUTHORIZED_PERSON }
            .groupBy { it.movementType }
            .let { grouped ->
                val delivered = grouped[MovementType.DELIVERY]?.sumOf { it.packageCount } ?: 0L
                val returned = grouped[MovementType.RETURN]?.sumOf { it.packageCount } ?: 0L
                val net = delivered - returned
                addLink("MAH", "OOV", net)
            }

        // Distributor → targets
        distAggregates.groupBy { it.purchaserType }.forEach { (purchaserType, list) ->
            val delivered = list.filter { it.movementType == MovementType.DELIVERY }.sumOf { it.packageCount }
            val returned = list.filter { it.movementType == MovementType.RETURN }.sumOf { it.packageCount }
            val net = delivered - returned

            if (net > 0) {
                val target = when (purchaserType) {
                    DistributorPurchaserType.DISTRIBUTOR_CR, DistributorPurchaserType.DISTRIBUTOR_EU, DistributorPurchaserType.DISTRIBUTOR_NON_EU -> "Distributor"
                    DistributorPurchaserType.PHARMACY -> "Pharmacy"
                    else -> {
                        addNode(purchaserType.name, purchaserLabels[purchaserType] ?: purchaserType.name)
                        purchaserType.name
                    }
                }
                addLink("Distributor", target, net)
            }
        }

        // Pharmacy → dispense types
        pharmacyAggregates.groupBy { it.dispenseType }.forEach { (dispenseType, list) ->
            val total = list.sumOf { it.packageCount }
            if (total > BigDecimal.ZERO) {
                val target = when (dispenseType) {
                    PharmacyDispenseType.PRESCRIPTION -> "Prescription"
                    PharmacyDispenseType.REQUISITION -> "Requisition"
                    PharmacyDispenseType.OTC -> "OTC"
                }
                val label = when (dispenseType) {
                    PharmacyDispenseType.PRESCRIPTION -> "Výdej na předpis"
                    PharmacyDispenseType.REQUISITION -> "Výdej na žádanku"
                    PharmacyDispenseType.OTC -> "Volný prodej"
                }
                addNode(target, label)
                addLink("Pharmacy", target, total.toLong())
            }
        }

        return DistributionSankeyResponse(
            includedMedicineProducts = included.map { MedicinalProductIdentificators(it.id!!, it.suklCode) },
            ignoredMedicineProducts = ignored.map { MedicinalProductIdentificators(it.id!!, it.suklCode) },
            dateFrom = req.dateFrom,
            dateTo = req.dateTo,
            calculationMode = req.calculationMode,
            nodes = nodes.toList(),
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

        val mahRaw = mahRepo.getMonthlyMahProductMovementCounts(productIds)
        val distRaw = distRepo.getMonthlyDistributorProductMovementCounts(productIds)
        val pharmRaw = pharmRepo.getMonthlyPharmacyProductDispenseCounts(productIds)

        fun getPeriod(year: Int, month: Int): String =
            if (request.timeGranularity == TimeGranularity.YEAR) year.toString()
            else "%04d-%02d".format(year, month)

        val allPeriods = mutableSetOf<String>()
        mahRaw.forEach { allPeriods.add(getPeriod(it.year, it.month)) }
        distRaw.forEach { allPeriods.add(getPeriod(it.year, it.month)) }
        pharmRaw.forEach { allPeriods.add(getPeriod(it.year, it.month)) }

        val series = allPeriods.sorted().map { period ->
            val flows = mutableListOf<DistributionFlowEntry>()

            // MAH → Distributor
            mahRaw.filter {
                getPeriod(it.year, it.month) == period && it.purchaserType == MahPurchaserType.DISTRIBUTOR
            }.groupBy { it.movementType }.let { grouped ->
                val delivered = grouped[MovementType.DELIVERY]?.sumOf { it.packageCount } ?: 0L
                val returned = grouped[MovementType.RETURN]?.sumOf { it.packageCount } ?: 0L
                val net = delivered - returned
                if (net > 0) flows += DistributionFlowEntry("MAH", "Distributor", net.toInt())
            }

            // MAH → OOV (Authorized Person)
            mahRaw.filter {
                getPeriod(it.year, it.month) == period && it.purchaserType == MahPurchaserType.AUTHORIZED_PERSON
            }.groupBy { it.movementType }.let { grouped ->
                val delivered = grouped[MovementType.DELIVERY]?.sumOf { it.packageCount } ?: 0L
                val returned = grouped[MovementType.RETURN]?.sumOf { it.packageCount } ?: 0L
                val net = delivered - returned
                if (net > 0) flows += DistributionFlowEntry("MAH", "OOV", net.toInt())
            }

            // Distributor → target
            distRaw.filter { getPeriod(it.year, it.month) == period }.groupBy { it.purchaserType }.forEach { (purchaserType, list) ->
                val delivered = list.filter { it.movementType == MovementType.DELIVERY }.sumOf { it.packageCount }
                val returned = list.filter { it.movementType == MovementType.RETURN }.sumOf { it.packageCount }
                val net = delivered - returned
                if (net > 0) {
                    val target = when (purchaserType) {
                        DistributorPurchaserType.DISTRIBUTOR_CR -> "Distributor"
                        DistributorPurchaserType.PHARMACY -> "Pharmacy"
                        else -> purchaserType.name
                    }
                    flows += DistributionFlowEntry("Distributor", target, net.toInt())
                }
            }

            // Pharmacy → dispense type
            pharmRaw.filter { getPeriod(it.year, it.month) == period }
                .groupBy { it.dispenseType }
                .forEach { (dispenseType, list) ->
                    val total = list.sumOf { it.packageCount }
                    if (total > BigDecimal.ZERO) {
                        val target = when (dispenseType) {
                            PharmacyDispenseType.PRESCRIPTION -> "Prescription"
                            PharmacyDispenseType.REQUISITION -> "Requisition"
                            PharmacyDispenseType.OTC -> "OTC"
                        }
                        flows += DistributionFlowEntry("Pharmacy", target, total.toInt())
                    }
                }

            DistributionTimeSeriesPeriodEntry(
                period = period,
                flows = flows
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
