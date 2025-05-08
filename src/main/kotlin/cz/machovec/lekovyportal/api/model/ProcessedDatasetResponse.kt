package cz.machovec.lekovyportal.api.model

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import java.time.OffsetDateTime

data class ProcessedDatasetResponse(
    val datasetType: DatasetType,
    val createdAt: OffsetDateTime,
    val year: Int,
    val month: Int
)
