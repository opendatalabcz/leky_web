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
        processorMap[DatasetType.MPD] = mpdFileProcessor
        processorMap[DatasetType.ERECEPT_PRESCRIPTION] = ereceptPrescriptionFileProcessor
        processorMap[DatasetType.ERECEPT_DISPENSE] = ereceptDispenseFileProcessor
        /*
        processorMap[DatasetType.DISTRIBUCE_REG] = regFileProcessor
        processorMap[DatasetType.DISTRIBUCE_DIS] = disFileProcessor
        processorMap[DatasetType.DISTRIBUCE_DIS_ZAHRANICI] = disAbroadFileProcessor
        processorMap[DatasetType.DISTRIBUCE_LEK] = lekFileProcessor
        */
    }

    fun resolve(datasetType: DatasetType): DatasetFileProcessor? {
        return processorMap[datasetType]
    }
}
