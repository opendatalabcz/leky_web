package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.domain.entity.DatasetType
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct

@Component
class FileProcessorResolver(
    private val regFileProcessor: RegFileProcessor,
    private val disFileProcessor: DisFileProcessor,
    private val disAbroadFileProcessor: DisAbroadFileProcessor,
    private val lekFileProcessor: LekFileProcessor,
    private val ereceptPrescriptionFileProcessor: EreceptPrescriptionFileProcessor,
    private val ereceptDispenseFileProcessor: EreceptDispenseFileProcessor,
    private val mpdFileProcessor: MpdFileProcessor,
) {

    private val processorMap = mutableMapOf<DatasetType, DatasetFileProcessor>()

    @PostConstruct
    fun init() {
        processorMap[DatasetType.MEDICINAL_PRODUCT_DATABASE] = mpdFileProcessor
        processorMap[DatasetType.ERECEPT_PRESCRIPTIONS] = ereceptPrescriptionFileProcessor
        processorMap[DatasetType.ERECEPT_DISPENSES] = ereceptDispenseFileProcessor
        processorMap[DatasetType.DISTRIBUTIONS_FROM_MAHS] = regFileProcessor
        processorMap[DatasetType.DISTRIBUTIONS_FROM_DISTRIBUTORS] = disFileProcessor
        processorMap[DatasetType.DISTRIBUTIONS_EXPORT_FROM_DISTRIBUTORS] = disAbroadFileProcessor
        processorMap[DatasetType.DISTRIBUTIONS_FROM_PHARMACIES] = lekFileProcessor
    }

    fun resolve(datasetType: DatasetType): DatasetFileProcessor? {
        return processorMap[datasetType]
    }
}
