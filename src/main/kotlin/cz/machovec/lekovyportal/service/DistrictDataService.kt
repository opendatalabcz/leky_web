package cz.machovec.lekovyportal.service

import cz.machovec.lekovyportal.api.dto.DistrictDataRequest
import org.springframework.stereotype.Service

@Service
class DistrictDataService {
    private val districtData = mapOf(
        "3203" to Pair(100, 70), // Kladno
        "3202" to Pair(90, 55),  // Beroun
        "3306" to Pair(80, 50),  // Prachatice
        "3307" to Pair(70, 30),  // Strakonice
        "3201" to Pair(85, 40),  // Benešov
        "3211" to Pair(75, 35),  // Příbram
        "3308" to Pair(65, 25),  // Tábor
        "3301" to Pair(95, 60),  // České Budějovice
        "3303" to Pair(85, 45),  // Jindřichův Hradec
        "3710" to Pair(75, 30),  // Třebíč
        "3713" to Pair(90, 50),  // Znojmo
        "3706" to Pair(95, 55),  // Hodonín
        "3704" to Pair(100, 65), // Břeclav
        "3806" to Pair(70, 20),  // Opava
        "3802" to Pair(85, 40),  // Frýdek-Místek
        "3805" to Pair(90, 60),  // Olomouc
        "3606" to Pair(80, 45),  // Pardubice
        "3602" to Pair(95, 65),  // Hradec Králové
        "3505" to Pair(85, 40),  // Liberec
        "3510" to Pair(90, 55),  // Ústí nad Labem
        "3503" to Pair(75, 35),  // Chomutov
        "3508" to Pair(65, 20),  // Most
        "3403" to Pair(95, 50),  // Karlovy Vary
        "3405" to Pair(100, 70), // Plzeň-město
        "3407" to Pair(80, 40),  // Plzeň-sever
        "3408" to Pair(90, 60),  // Rokycany
        "3402" to Pair(85, 45),  // Cheb
        "3409" to Pair(75, 35),  // Sokolov
        "3401" to Pair(70, 25)   // Domažlice
    )

    fun aggregateByDistrict(request: DistrictDataRequest): Map<String, Int> {
        return when (request.filterType) {
            "prescribed" -> districtData.mapValues { it.value.first }
            "dispensed" -> districtData.mapValues { it.value.second }
            "difference" -> districtData.mapValues { it.value.first - it.value.second }
            else -> throw IllegalArgumentException("Invalid filter type: ${request.filterType}")
        }
    }
}
