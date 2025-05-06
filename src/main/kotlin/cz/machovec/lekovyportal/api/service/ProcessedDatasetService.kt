package cz.machovec.lekovyportal.api.service

import cz.machovec.lekovyportal.api.model.ProcessedDatasetResponse
import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.core.repository.ProcessedDatasetRepository
import org.springframework.stereotype.Service

@Service
class ProcessedDatasetService(
    private val repository: ProcessedDatasetRepository
) {
    fun getLatestDataset(types: List<DatasetType>): ProcessedDatasetResponse? {
        return repository.findFirstByDatasetTypeInOrderByCreatedAtDesc(types)
            ?.let {
                ProcessedDatasetResponse(
                    datasetType = it.datasetType,
                    createdAt = it.createdAt,
                    year = it.year,
                    month = it.month
                )
            }
    }
}
