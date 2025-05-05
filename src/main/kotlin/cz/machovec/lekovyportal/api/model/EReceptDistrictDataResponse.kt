package cz.machovec.lekovyportal.api.model

import cz.machovec.lekovyportal.api.model.enums.CalculationMode
import cz.machovec.lekovyportal.api.model.enums.EReceptDataTypeAggregation
import cz.machovec.lekovyportal.api.model.enums.NormalisationMode

data class EReceptDistrictDataResponse(
    val aggregationType: EReceptDataTypeAggregation,
    val calculationMode: CalculationMode,
    val normalisationMode: NormalisationMode,
    val dateFrom: String?,
    val dateTo: String?,
    val districtValues: Map<String, Int>,
    val includedMedicineProducts: List<MedicineProductInfo>,
    val ignoredMedicineProducts: List<MedicineProductInfo>,
    val summary: SummaryValues
)

data class MedicineProductInfo(
    val id: Long,
    val suklCode: String
)