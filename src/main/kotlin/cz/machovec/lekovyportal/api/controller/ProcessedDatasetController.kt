package cz.machovec.lekovyportal.api.controller

import cz.machovec.lekovyportal.api.model.ProcessedDatasetResponse
import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.api.service.ProcessedDatasetService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/processed-datasets")
class ProcessedDatasetController(
    private val service: ProcessedDatasetService
) {
    @GetMapping("/latest")
    fun getLatestDataset(@RequestParam types: List<DatasetType>): ProcessedDatasetResponse? {
        return service.getLatestDataset(types)
    }
}
