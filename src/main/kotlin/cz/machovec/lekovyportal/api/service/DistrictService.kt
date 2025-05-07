package cz.machovec.lekovyportal.api.service

import cz.machovec.lekovyportal.core.repository.erecept.DistrictRepository
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service

@Service
class DistrictService(private val districtRepository: DistrictRepository) {

    private val districtMap = mutableMapOf<String, String>()

    @PostConstruct
    fun loadDistricts() {
        districtRepository.findAll().forEach { district ->
            districtMap[district.code] = district.name
        }
    }
}
