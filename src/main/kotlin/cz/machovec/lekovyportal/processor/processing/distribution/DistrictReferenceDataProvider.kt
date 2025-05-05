package cz.machovec.lekovyportal.processor.processing.distribution

import cz.machovec.lekovyportal.core.domain.erecept.District
import cz.machovec.lekovyportal.core.repository.erecept.DistrictRepository
import org.springframework.stereotype.Component

@Component
class DistrictReferenceDataProvider(
    private val districtRepository: DistrictRepository
) {

    private val districts: Map<String, District> by lazy {
        districtRepository.findAll()
            .associateBy { it.code }
    }

    fun getDistrictMap(): Map<String, District> = districts
}
