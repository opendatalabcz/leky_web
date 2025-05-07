package cz.machovec.lekovyportal.api.model.erecept

import cz.machovec.lekovyportal.api.model.mpd.MedicinalProductIdentificators
import cz.machovec.lekovyportal.api.model.enums.CalculationMode
import cz.machovec.lekovyportal.api.model.enums.EReceptDataTypeAggregation
import cz.machovec.lekovyportal.api.model.enums.NormalisationMode

data class PrescriptionDispenseByDistrictAggregateResponse(
    val aggregationType: EReceptDataTypeAggregation,
    val calculationMode: CalculationMode,
    val normalisationMode: NormalisationMode,
    val dateFrom: String?,
    val dateTo: String?,
    val districtValues: Map<String, Int>,
    val includedMedicineProducts: List<MedicinalProductIdentificators>,
    val ignoredMedicineProducts: List<MedicinalProductIdentificators>,
    val summary: SummaryValues
)