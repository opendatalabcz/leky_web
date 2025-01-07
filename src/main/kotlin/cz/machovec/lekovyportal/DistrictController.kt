package cz.machovec.lekovyportal

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class DistrictController {

    private val districtData = mapOf(
        "Kladno" to Pair(100, 70),
        "Beroun" to Pair(90, 55),
        "Prachatice" to Pair(80, 50),
        "Strakonice" to Pair(70, 30),
        "Benešov" to Pair(85, 40),
        "Příbram" to Pair(75, 35),
        "Tábor" to Pair(65, 25),
        "České Budějovice" to Pair(95, 60),
        "Jindřichův Hradec" to Pair(85, 45),
        "Třebíč" to Pair(75, 30),
        "Znojmo" to Pair(90, 50),
        "Hodonín" to Pair(95, 55),
        "Břeclav" to Pair(100, 65),
        "Opava" to Pair(70, 20),
        "Frýdek-Místek" to Pair(85, 40),
        "Olomouc" to Pair(90, 60),
        "Pardubice" to Pair(80, 45),
        "Hradec Králové" to Pair(95, 65),
        "Liberec" to Pair(85, 40),
        "Ústí nad Labem" to Pair(90, 55),
        "Chomutov" to Pair(75, 35),
        "Most" to Pair(65, 20),
        "Karlovy Vary" to Pair(95, 50),
        "Plzeň-město" to Pair(100, 70),
        "Plzeň-sever" to Pair(80, 40),
        "Rokycany" to Pair(90, 60),
        "Cheb" to Pair(85, 45),
        "Sokolov" to Pair(75, 35),
        "Domažlice" to Pair(70, 25)
    )

    @GetMapping("/district-data")
    fun getDistrictData(@RequestParam filter: String): Map<String, Int> {
        return when (filter) {
            "prescribed" -> districtData.mapValues { it.value.first }
            "dispensed" -> districtData.mapValues { it.value.second }
            "difference" -> districtData.mapValues { it.value.first - it.value.second }
            else -> throw IllegalArgumentException("Invalid filter type: $filter")
        }
    }
}
