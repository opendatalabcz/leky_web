package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.domain.entity.DatasetType
import cz.machovec.lekovyportal.processor.dist.DistExportFromDistributorsProcessor
import cz.machovec.lekovyportal.processor.dist.DistFromDistributorsProcessor
import cz.machovec.lekovyportal.processor.dist.DistFromMahsProcessor
import cz.machovec.lekovyportal.processor.dist.DistFromPharmaciesProcessor
import cz.machovec.lekovyportal.processor.erecept.EreceptDispenseProcessor
import cz.machovec.lekovyportal.processor.erecept.EreceptPrescriptionProcessor
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct

@Component
class DatasetProcessorResolver(
    private val regFileProcessor: DistFromMahsProcessor,
    private val disFileProcessor: DistFromDistributorsProcessor,
    private val disAbroadFileProcessor: DistExportFromDistributorsProcessor,
    private val lekFileProcessor: DistFromPharmaciesProcessor,
    private val ereceptPrescriptionFileProcessor: EreceptPrescriptionProcessor,
    private val ereceptDispenseFileProcessor: EreceptDispenseProcessor,
    private val mpdFileProcessor: MpdProcessor,
) {

    private val processorMap = mutableMapOf<DatasetType, DatasetProcessor>()

    @PostConstruct
    fun init() {
        processorMap[DatasetType.MEDICINAL_PRODUCT_DATABASE] = mpdFileProcessor
        /*
        processorMap[DatasetType.ERECEPT_PRESCRIPTIONS] = ereceptPrescriptionFileProcessor
        processorMap[DatasetType.ERECEPT_DISPENSES] = ereceptDispenseFileProcessor
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
