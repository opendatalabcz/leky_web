package cz.machovec.lekovyportal.service

import cz.machovec.lekovyportal.domain.repository.DistrictRepository
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

    fun getDistrictName(code: String): String {
        return districtMap[code] ?: "Neznámý okres"
    }
}
