package cz.machovec.lekovyportal.service

import cz.machovec.lekovyportal.api.DistributionTimeSeriesEntry
import cz.machovec.lekovyportal.api.DistributionTimeSeriesRequest
import cz.machovec.lekovyportal.api.DistributionTimeSeriesResponse
import cz.machovec.lekovyportal.api.dto.Granularity
import cz.machovec.lekovyportal.api.dto.MedicineProductInfo
import cz.machovec.lekovyportal.domain.entity.distribution.DistributorPurchaserType
import cz.machovec.lekovyportal.domain.entity.distribution.MahPurchaserType
import cz.machovec.lekovyportal.domain.entity.distribution.MovementType
import cz.machovec.lekovyportal.domain.repository.dist.DistFromDistributorsRepository
import cz.machovec.lekovyportal.domain.repository.dist.DistFromMahsRepository
import cz.machovec.lekovyportal.domain.repository.dist.DistFromPharmaciesRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMedicinalProductRepository
import org.springframework.stereotype.Service

@Service
class DistributionTimeSeriesService(
    private val mahRepo: DistFromMahsRepository,
    private val distRepo: DistFromDistributorsRepository,
    private val pharmRepo: DistFromPharmaciesRepository,
    private val productRepo: MpdMedicinalProductRepository
) {

    fun getTimeSeries(request: DistributionTimeSeriesRequest): DistributionTimeSeriesResponse {
        val allProducts = productRepo.findAllByIdIn(request.medicinalProductIds)
        val (included, ignored) = allProducts.partition { it.id != null }
        val productIds = included.mapNotNull { it.id }

        // Načíst všechna relevantní data v surové podobě
        val mahRaw = mahRepo.findMonthlyAggregates(productIds)
        val distRaw = distRepo.findMonthlyAggregates(productIds)
        val pharmRaw = pharmRepo.findMonthlyAggregates(productIds)

        // Rozdělit dle zvolené granularity
        fun getPeriod(year: Int, month: Int): String =
            if (request.granularity == Granularity.YEAR) year.toString()
            else "%04d-%02d".format(year, month)

        // Agregace MAH → Distributor
        val mahGrouped = mahRaw
            .filter { it.purchaserType == MahPurchaserType.DISTRIBUTOR }
            .groupBy { getPeriod(it.year, it.month) }

        val mahFlow = mahGrouped.mapValues { (_, list) ->
            val del = list.filter { it.movementType == MovementType.DELIVERY }.sumOf { it.totalCount }
            val ret = list.filter { it.movementType == MovementType.RETURN }.sumOf { it.totalCount }
            del - ret
        }

        // Agregace Distributor → Pharmacy
        val distGrouped = distRaw
            .filter { it.purchaserType == DistributorPurchaserType.PHARMACY }
            .groupBy { getPeriod(it.year, it.month) }

        val distFlow = distGrouped.mapValues { (_, list) ->
            val del = list.filter { it.movementType == MovementType.DELIVERY }.sumOf { it.totalCount }
            val ret = list.filter { it.movementType == MovementType.RETURN }.sumOf { it.totalCount }
            del - ret
        }

        // Agregace Pharmacy → Patient
        val pharmGrouped = pharmRaw.groupBy { getPeriod(it.year, it.month) }
        val pharmFlow = pharmGrouped.mapValues { (_, list) ->
            list.sumOf { it.packageCount }.toInt()
        }

        // Sjednotit všechna období
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
            granularity = request.granularity,
            series = series,
            includedMedicineProducts = included.map { MedicineProductInfo(it.id!!, it.suklCode) },
            ignoredMedicineProducts = ignored.map { MedicineProductInfo(it.id!!, it.suklCode) }
        )
    }
}
