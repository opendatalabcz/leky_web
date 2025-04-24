package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.importer.processing.distribution.DistributionBundleJob
import cz.machovec.lekovyportal.importer.processing.erecept.EreceptBundleJob
import cz.machovec.lekovyportal.importer.processing.mpd.MpdBundleJob
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
