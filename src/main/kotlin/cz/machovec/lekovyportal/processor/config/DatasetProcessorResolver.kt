package cz.machovec.lekovyportal.processor.config

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.processor.processing.DatasetProcessor
import cz.machovec.lekovyportal.processor.processing.distribution.DistributionProcessor
import cz.machovec.lekovyportal.processor.processing.erecept.EreceptProcessor
import cz.machovec.lekovyportal.processor.processing.mpd.MpdProcessor
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class DatasetProcessorResolver(
    private val mpdProcessor: MpdProcessor,
    private val ereceptProcessor: EreceptProcessor,
    private val distributionProcessor: DistributionProcessor,
) {

    private val processorMap = mutableMapOf<DatasetType, DatasetProcessor>()

    @PostConstruct
    fun init() {
        processorMap[DatasetType.MEDICINAL_PRODUCT_DATABASE] = mpdProcessor
        processorMap[DatasetType.ERECEPT_PRESCRIPTIONS] = ereceptProcessor
        processorMap[DatasetType.ERECEPT_DISPENSES] = ereceptProcessor
        processorMap[DatasetType.DISTRIBUTIONS_FROM_MAHS] = distributionProcessor
        processorMap[DatasetType.DISTRIBUTIONS_FROM_DISTRIBUTORS] = distributionProcessor
        processorMap[DatasetType.DISTRIBUTIONS_EXPORT_FROM_DISTRIBUTORS] = distributionProcessor
        processorMap[DatasetType.DISTRIBUTIONS_FROM_PHARMACIES] = distributionProcessor
    }

    fun resolve(datasetType: DatasetType): DatasetProcessor? {
        return processorMap[datasetType]
    }
}
