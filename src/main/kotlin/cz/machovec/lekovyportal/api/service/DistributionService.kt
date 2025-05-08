package cz.machovec.lekovyportal.api.service

import DistributionSankeyRequest
import DistributionSankeyResponse
import DistributionTimeSeriesRequest
import DistributionTimeSeriesResponse
import cz.machovec.lekovyportal.api.logic.DoseUnitConverterFactory
import cz.machovec.lekovyportal.api.logic.distribution.SankeyDiagramAssembler
import cz.machovec.lekovyportal.api.logic.distribution.TimeSeriesAssembler
import cz.machovec.lekovyportal.api.model.mpd.MedicinalProductIdentificators
import cz.machovec.lekovyportal.core.repository.distribution.*
import cz.machovec.lekovyportal.core.repository.mpd.MpdMedicinalProductRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class DistributionService(
    private val mahRepo: DistFromMahsRepository,
    private val distRepo: DistFromDistributorsRepository,
    private val pharmRepo: DistFromPharmaciesRepository,
    private val medicinalProductRepo: MpdMedicinalProductRepository,
    private val converterFactory: DoseUnitConverterFactory,
    private val sankeyAssembler: SankeyDiagramAssembler,
    private val timeSeriesAssembler: TimeSeriesAssembler
) {

    /** PUBLIC API **/

    fun getSankeyDiagram(req: DistributionSankeyRequest): DistributionSankeyResponse {

        // Step 1: Load products (by IDs + registration numbers) and build DDD lookup
        val (included, ignored) = loadProducts(req.medicinalProductIds, req.registrationNumbers)
        val dddPerProduct = included.associate { it.id!! to (it.dailyDosePackaging ?: BigDecimal.ZERO) }
        val productIds    = included.mapNotNull { it.id }

        // Step 2: Fetch aggregated rows from all three reporting sources
        val (fromY, fromM) = req.dateFrom.split("-").let { it[0].toInt() to it[1].toInt() }
        val (toY,   toM)   = req.dateTo.split("-").let { it[0].toInt() to it[1].toInt() }

        val mahRows   = mahRepo.getAggregateProductMovementCounts(productIds, fromY, fromM, toY, toM)
        val distRows  = distRepo.getAggregateDistributorProductMovementCounts(productIds, fromY, fromM, toY, toM)
        val pharmRows = pharmRepo.getAggregatePharmacyProductDispenseCounts(productIds, fromY, fromM, toY, toM)

        // Step 3: Convert units (PACKAGES ↔ DDD) and assemble nodes + links
        val converter      = converterFactory.of(req.medicinalUnitMode)
        val (nodes, links) = sankeyAssembler.assemble(mahRows, distRows, pharmRows, converter, dddPerProduct)

        // Step 4: Build response DTO
        return DistributionSankeyResponse(
            includedMedicineProducts = included.toDto(),
            ignoredMedicineProducts  = ignored.toDto(),
            dateFrom          = req.dateFrom,
            dateTo            = req.dateTo,
            medicinalUnitMode = req.medicinalUnitMode,
            nodes             = nodes,
            links             = links
        )
    }

    fun getTimeSeries(req: DistributionTimeSeriesRequest): DistributionTimeSeriesResponse {

        // Step 1: Load products and DDD lookup
        val (included, ignored) = loadProducts(req.medicinalProductIds, req.registrationNumbers)
        val dddPerProduct = included.associate { it.id!! to (it.dailyDosePackaging ?: BigDecimal.ZERO) }
        val productIds    = included.mapNotNull { it.id }

        // Step 2: Fetch raw monthly rows for all reporters
        val mahRows   = mahRepo.getMonthlyMahProductMovementCounts(productIds)
        val distRows  = distRepo.getMonthlyDistributorProductMovementCounts(productIds)
        val pharmRows = pharmRepo.getMonthlyPharmacyProductDispenseCounts(productIds)

        // Step 3: Convert units + assemble per-period flows
        val converter = converterFactory.of(req.medicinalUnitMode)
        val series    = timeSeriesAssembler.assemble(
            mahRows, distRows, pharmRows,
            req.timeGranularity, converter, dddPerProduct
        )

        // Step 4: Build response DTO
        return DistributionTimeSeriesResponse(
            includedMedicineProducts = included.toDto(),
            ignoredMedicineProducts  = ignored.toDto(),
            dateFrom          = req.dateFrom,
            dateTo            = req.dateTo,
            medicinalUnitMode = req.medicinalUnitMode,
            timeGranularity   = req.timeGranularity,
            series            = series
        )
    }

    /** PRIVATE UTILITIES **/

    /** Loads products by IDs + registration numbers and returns Pair<included, ignored>. */
    private fun loadProducts(ids: List<Long>, regNumbers: List<String>) =
        run {
            val byId  = if (ids.isNotEmpty()) medicinalProductRepo.findAllByIdIn(ids) else emptyList()
            val byReg = if (regNumbers.isNotEmpty()) medicinalProductRepo.findAllByRegistrationNumberIn(regNumbers) else emptyList()
            (byId + byReg).distinctBy { it.id }.partition { it.id != null }
        }

    /** Converts MPD entities to lightweight DTOs for the response payload. */
    private fun List<cz.machovec.lekovyportal.core.domain.mpd.MpdMedicinalProduct>.toDto() =
        map { MedicinalProductIdentificators(it.id!!, it.suklCode) }
}
