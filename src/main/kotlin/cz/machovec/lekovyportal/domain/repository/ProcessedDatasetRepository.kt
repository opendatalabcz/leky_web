package cz.machovec.lekovyportal.domain.repository

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.ProcessedDataset
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProcessedDatasetRepository : JpaRepository<ProcessedDataset, Long> {
    fun existsByDatasetTypeAndYearAndMonth(
        datasetType: DatasetType,
        year: Int,
        month: Int
    ): Boolean
}
