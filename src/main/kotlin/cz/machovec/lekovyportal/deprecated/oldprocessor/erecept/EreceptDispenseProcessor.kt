package cz.machovec.lekovyportal.deprecated.oldprocessor.erecept

/*
@Service
class EreceptDispenseProcessor(
    private val repository: EreceptDispenseRepository,
    processedDatasetRepository: ProcessedDatasetRepository,
    private val referenceDataProvider: MpdReferenceDataProvider
) : BaseEreceptProcessor<EreceptDispense>(
    datasetType = DatasetType.ERECEPT_DISPENSES,
    processedDatasetRepository = processedDatasetRepository,
    batchInsert = { records -> repository.batchInsert(records, batchSize = 1000) },
    parseCsvRecord = fun(cols: List<String>): CsvRecordResult<EreceptDispense>? {
        val districtCode = cols[0]
        val year = cols[2].toIntOrNull() ?: return null
        val month = cols[3].toIntOrNull() ?: return null
        val suklCode = cols[4]
        val quantity = cols[9].toIntOrNull() ?: return null

        val medicinalProduct = referenceDataProvider.getMedicinalProducts()[suklCode]
            ?: return null

        return CsvRecordResult(
            EreceptDispense(
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

 */
