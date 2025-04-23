package cz.machovec.lekovyportal

import java.time.LocalDate

data class MpdValidity(
    val validFrom: LocalDate,
    val validTo: LocalDate
)