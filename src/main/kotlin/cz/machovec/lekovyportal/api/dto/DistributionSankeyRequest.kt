package cz.machovec.lekovyportal.api.dto

import cz.machovec.lekovyportal.api.enum.CalculationMode

data class DistributionSankeyRequest(
    val dateFrom: String,
    val dateTo: String,
    val medicinalProductIds: List<Long>,
    val mode: Mode = Mode.PACKAGES
)

enum class Mode {
    DAILY_DOSES,
    PACKAGES;

    fun toCalculationMode(): CalculationMode = when (this) {
        DAILY_DOSES -> CalculationMode.DAILY_DOSES
        PACKAGES -> CalculationMode.PACKAGES
    }
}
