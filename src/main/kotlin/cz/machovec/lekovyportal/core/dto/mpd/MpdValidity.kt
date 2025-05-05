package cz.machovec.lekovyportal.core.dto.mpd

import java.time.LocalDate

data class MpdValidity(
    val validFrom: LocalDate,
    val validTo: LocalDate
)