package cz.machovec.lekovyportal.core.repository

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.core.domain.dataset.ProcessedDataset
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProcessedDatasetRepository : JpaRepository<ProcessedDataset, Long> {
    fun existsByDatasetTypeAndYearAndMonth(
        datasetType: DatasetType,
        year: Int,
        month: Int
    ): Boolean

    fun findAllByDatasetTypeAndYear(
        datasetType: DatasetType,
        year: Int
    ): List<ProcessedDataset>

    fun findFirstByDatasetTypeInOrderByCreatedAtDesc(types: List<DatasetType>): ProcessedDataset?

    fun existsByDatasetType(datasetType: DatasetType): Boolean
}
