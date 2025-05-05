package cz.machovec.lekovyportal.processor.config

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.processor.processing.DatasetProcessor
import cz.machovec.lekovyportal.processor.processing.distribution.DistributionBundleJob
import cz.machovec.lekovyportal.processor.processing.erecept.EreceptBundleJob
import cz.machovec.lekovyportal.processor.processing.mpd.MpdBundleJob
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class DatasetProcessorResolver(
    private val mpdBundleJob: MpdBundleJob,
    private val ereceptBundleJob: EreceptBundleJob,
    private val distributionBundleJob: DistributionBundleJob,
) {

    private val processorMap = mutableMapOf<DatasetType, DatasetProcessor>()

    @PostConstruct
    fun init() {
        processorMap[DatasetType.MEDICINAL_PRODUCT_DATABASE] = mpdBundleJob
        processorMap[DatasetType.ERECEPT_PRESCRIPTIONS] = ereceptBundleJob
        processorMap[DatasetType.ERECEPT_DISPENSES] = ereceptBundleJob
        processorMap[DatasetType.DISTRIBUTIONS_FROM_MAHS] = distributionBundleJob
        processorMap[DatasetType.DISTRIBUTIONS_FROM_DISTRIBUTORS] = distributionBundleJob
        processorMap[DatasetType.DISTRIBUTIONS_EXPORT_FROM_DISTRIBUTORS] = distributionBundleJob
        processorMap[DatasetType.DISTRIBUTIONS_FROM_PHARMACIES] = distributionBundleJob
    }

    fun resolve(datasetType: DatasetType): DatasetProcessor? {
        return processorMap[datasetType]
    }
}
