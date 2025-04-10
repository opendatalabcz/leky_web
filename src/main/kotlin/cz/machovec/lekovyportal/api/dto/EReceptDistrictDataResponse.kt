package cz.machovec.lekovyportal.api.dto

import cz.machovec.lekovyportal.api.enum.CalculationMode
import cz.machovec.lekovyportal.api.enum.EReceptDataTypeAggregation
import cz.machovec.lekovyportal.api.enum.NormalisationMode

data class EReceptDistrictDataResponse(
    val aggregationType: EReceptDataTypeAggregation,
    val calculationMode: CalculationMode,
    val normalisationMode: NormalisationMode,
    val dateFrom: String?, // format yyyy-MM
    val dateTo: String?,   // format yyyy-MM
    val districtValues: Map<String, Int>, // districtCode -> value
    val includedMedicineProducts: List<MedicineProductInfo>,
    val ignoredMedicineProducts: List<MedicineProductInfo>
)

data class MedicineProductInfo(
    val id: Long,
    val suklCode: String
)