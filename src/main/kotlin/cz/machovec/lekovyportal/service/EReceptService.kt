package cz.machovec.lekovyportal.service

import cz.machovec.lekovyportal.api.dto.EReceptDataResponse
import cz.machovec.lekovyportal.domain.repository.DistrictRepository
import cz.machovec.lekovyportal.domain.repository.EreceptDispenseRepository
import cz.machovec.lekovyportal.domain.repository.EreceptPrescriptionRepository
import org.springframework.stereotype.Service

@Service
class EReceptService(
    private val ereceptPrescriptionRepository: EreceptPrescriptionRepository,
    private val ereceptDispenseRepository: EreceptDispenseRepository,
    private val districtRepository: DistrictRepository
) {
    fun getDistrictData(suklCode: String, year: Int, month: Int): List<EReceptDataResponse> {
        val prescribedData = ereceptPrescriptionRepository.findBySuklCodeAndYearAndMonth(suklCode, year, month)
        val dispensedData = ereceptDispenseRepository.findBySuklCodeAndYearAndMonth(suklCode, year, month)

        val prescribedMap = prescribedData.groupBy { it.districtCode }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }

        val dispensedMap = dispensedData.groupBy { it.districtCode }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }

        val allDistrictCodes = prescribedMap.keys + dispensedMap.keys

        val districtMap = districtRepository.findAll().associateBy { it.code }

        return allDistrictCodes.map { districtCode ->
            val districtName = districtMap[districtCode]?.name ?: "Neznámý okres"
            EReceptDataResponse(
                districtName = districtName,
                prescribed = prescribedMap[districtCode] ?: 0,
                dispensed = dispensedMap[districtCode] ?: 0,
                difference = (prescribedMap[districtCode] ?: 0) - (dispensedMap[districtCode] ?: 0)
            )
        }
    }
}
