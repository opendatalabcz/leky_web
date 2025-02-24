package cz.machovec.lekovyportal.processor

import cz.machovec.lekovyportal.domain.entity.DatasetType
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct

@Component
class FileProcessorResolver(
    private val reg13FileProcessor: Reg13FileProcessor,
    private val ereceptPredpisFileProcessor: EreceptPredpisFileProcessor,
    private val ereceptDispenseFileProcessor: EreceptDispenseFileProcessor,
) {

    private val processorMap = mutableMapOf<DatasetType, DatasetFileProcessor>()

    @PostConstruct
    fun init() {
        processorMap[DatasetType.DISTRIBUCE_REG] = reg13FileProcessor
        processorMap[DatasetType.ERECEPT_PREDPIS] = ereceptPredpisFileProcessor
        processorMap[DatasetType.ERECEPT_VYDEJ] = ereceptDispenseFileProcessor
    }

    fun resolve(datasetType: DatasetType): DatasetFileProcessor? {
        return processorMap[datasetType]
    }
}
