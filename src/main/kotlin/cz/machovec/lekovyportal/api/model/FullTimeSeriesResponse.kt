package cz.machovec.lekovyportal.api.model

import cz.machovec.lekovyportal.api.model.enums.CalculationMode
import cz.machovec.lekovyportal.api.model.enums.EReceptDataTypeAggregation
import cz.machovec.lekovyportal.api.model.enums.NormalisationMode

data class FullTimeSeriesResponse(
    val aggregationType: EReceptDataTypeAggregation,
    val calculationMode: CalculationMode,
    val normalisationMode: NormalisationMode,
    val granularity: Granularity,
    val district: String?, // null = celá ČR
    val series: List<FullTimeSeriesEntry>,
    val includedMedicineProducts: List<MedicinalProductIdentificators>,
    val ignoredMedicineProducts: List<MedicinalProductIdentificators>
)

data class FullTimeSeriesEntry(
    val period: String, // "2023-05" nebo "2023"
    val prescribed: Int,
    val dispensed: Int,
    val difference: Int
)

enum class Granularity {
    MONTH,
    YEAR
}
