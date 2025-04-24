package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.importer.processing.erecept.EreceptBundleJob
import cz.machovec.lekovyportal.importer.processing.mpd.MpdBundleJob
import cz.machovec.lekovyportal.processor.dist.DistExportFromDistributorsProcessor
import cz.machovec.lekovyportal.processor.dist.DistFromDistributorsProcessor
import cz.machovec.lekovyportal.processor.dist.DistFromMahsProcessor
import cz.machovec.lekovyportal.processor.dist.DistFromPharmaciesProcessor
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class DatasetProcessorResolver(
    private val regFileProcessor: DistFromMahsProcessor,
    private val disFileProcessor: DistFromDistributorsProcessor,
    private val disAbroadFileProcessor: DistExportFromDistributorsProcessor,
    private val lekFileProcessor: DistFromPharmaciesProcessor,
    private val mpdBundleJob: MpdBundleJob,
    private val ereceptBundleJob: EreceptBundleJob,
) {

    private val processorMap = mutableMapOf<DatasetType, DatasetProcessor>()

    @PostConstruct
    fun init() {
        processorMap[DatasetType.MEDICINAL_PRODUCT_DATABASE] = mpdBundleJob
        processorMap[DatasetType.ERECEPT_PRESCRIPTIONS] = ereceptBundleJob
        processorMap[DatasetType.ERECEPT_DISPENSES] = ereceptBundleJob
        /*
        processorMap[DatasetType.DISTRIBUTIONS_FROM_MAHS] = regFileProcessor
        processorMap[DatasetType.DISTRIBUTIONS_FROM_DISTRIBUTORS] = disFileProcessor
        processorMap[DatasetType.DISTRIBUTIONS_EXPORT_FROM_DISTRIBUTORS] = disAbroadFileProcessor
        processorMap[DatasetType.DISTRIBUTIONS_FROM_PHARMACIES] = lekFileProcessor
         */
    }

    fun resolve(datasetType: DatasetType): DatasetProcessor? {
        return processorMap[datasetType]
    }
}
