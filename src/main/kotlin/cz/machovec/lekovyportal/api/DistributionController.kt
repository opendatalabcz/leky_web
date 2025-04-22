package cz.machovec.lekovyportal.api

import cz.machovec.lekovyportal.api.dto.DistributionSankeyRequest
import cz.machovec.lekovyportal.api.dto.DistributionSankeyResponse
import cz.machovec.lekovyportal.service.DistributionService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api/distribution")
class DistributionController(
    private val distributionService: DistributionService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/graph")
    fun getDistributionGraph(
        @RequestBody request: DistributionSankeyRequest
    ): DistributionSankeyResponse {
        logger.info(
            "Distribution Sankey request — productIds=${request.medicinalProductIds.joinToString(",")}, " +
                    "from=${request.dateFrom}, to=${request.dateTo}"
        )

        val response = distributionService.buildMahFlowSankey(request)

        logger.info("Returning Sankey response with ${response.nodes.size} nodes and ${response.links.size} links")

        val nodeLabels = response.nodes.joinToString(", ") { "${it.id} (${it.label})" }
        val linkDescriptions = response.links.joinToString(", ") {
            "${it.source} → ${it.target}: ${it.value}"
        }

        logger.info("Nodes: $nodeLabels")
        logger.info("Links: $linkDescriptions")

        return response
    }

    @PostMapping("/graph/from-distributors")
    fun getDistributorFlowGraph(
        @RequestBody request: DistributionSankeyRequest
    ): DistributionSankeyResponse {
        logger.info(
            "Distribution Sankey (distributor flow) — productIds=${request.medicinalProductIds.joinToString(",")}, " +
                    "from=${request.dateFrom}, to=${request.dateTo}"
        )

        val response = distributionService.buildDistributorFlowSankey(request)

        logger.info("Returning Distributor Sankey with ${response.nodes.size} nodes and ${response.links.size} links")

        val nodeLabels = response.nodes.joinToString(", ") { "${it.id} (${it.label})" }
        val linkDescriptions = response.links.joinToString(", ") {
            "${it.source} → ${it.target}: ${it.value}"
        }

        logger.info("Nodes: $nodeLabels")
        logger.info("Links: $linkDescriptions")

        return response
    }
}
