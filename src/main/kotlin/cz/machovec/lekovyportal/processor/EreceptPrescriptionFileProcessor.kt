package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.domain.entity.EreceptPrescription
import cz.machovec.lekovyportal.domain.repository.EreceptPrescriptionRepository
import cz.machovec.lekovyportal.domain.repository.ProcessedDatasetRepository
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider
import org.springframework.stereotype.Service

@Service
class EreceptPrescriptionFileProcessor(
    private val repository: EreceptPrescriptionRepository,
    processedDatasetRepository: ProcessedDatasetRepository,
    private val referenceDataProvider: MpdReferenceDataProvider
) : BaseEreceptFileProcessor<EreceptPrescription>(
    datasetType = DatasetType.ERECEPT_PRESCRIPTION,
    processedDatasetRepository = processedDatasetRepository,
    batchInsert = { records -> repository.batchInsert(records, batchSize = 1000) },
    parseCsvRecord = fun(cols: List<String>): CsvRecordResult<EreceptPrescription>? {
        val districtCode = cols[0]
        val year = cols[2].toIntOrNull() ?: return null
        val month = cols[3].toIntOrNull() ?: return null
        val suklCode = cols[4]
        val quantity = cols[9].toIntOrNull() ?: return null

        val medicinalProduct = referenceDataProvider.getMedicinalProducts()[suklCode]
            ?: return null

        return CsvRecordResult(
            EreceptPrescription(
                districtCode = districtCode,
                year = year,
                month = month,
                medicinalProduct = medicinalProduct,
                quantity = quantity
            )
        )
    },
    mergeByQuantity = { a, b -> a.copy(quantity = a.quantity + b.quantity) },
    extractAggregationKey = { "${it.districtCode}-${it.year}-${it.month}-${it.medicinalProduct.suklCode}" }
)
