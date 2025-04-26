package cz.machovec.lekovyportal.importer.processing.distribution

import cz.machovec.lekovyportal.domain.entity.District
import cz.machovec.lekovyportal.domain.repository.DistrictRepository
import org.springframework.stereotype.Component

@Component
class DistrictReferenceDataProvider(
    private val districtRepository: DistrictRepository
) {

    private val districtMap: Map<String, District> by lazy {
        districtRepository.findAll()
            .associateBy { it.code }
    }

    fun getDistrictMap(): Map<String, District> = districtMap
}
