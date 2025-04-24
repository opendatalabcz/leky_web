package cz.machovec.lekovyportal.importer.mapper.erecept

import cz.machovec.lekovyportal.domain.entity.EreceptDispense
import cz.machovec.lekovyportal.domain.entity.EreceptPrescription
import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProduct
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider

data class EreceptRawData(
    val districtCode: String,
    val year: Int,
    val month: Int,
    val suklCode: String,
    val quantity: Int
)

fun EreceptRawData.toDispenseEntity(
    referenceDataProvider: MpdReferenceDataProvider
): EreceptDispense? {
    val medicinalProduct: MpdMedicinalProduct = referenceDataProvider.getMedicinalProducts()[suklCode]
        ?: return null

    return EreceptDispense(
        districtCode = districtCode,
        year = year,
        month = month,
        medicinalProduct = medicinalProduct,
        quantity = quantity
    )
}

fun EreceptRawData.toPrescriptionEntity(
    referenceDataProvider: MpdReferenceDataProvider
): EreceptPrescription? {
    val medicinalProduct: MpdMedicinalProduct = referenceDataProvider.getMedicinalProducts()[suklCode]
        ?: return null

    return EreceptPrescription(
        districtCode = districtCode,
        year = year,
        month = month,
        medicinalProduct = medicinalProduct,
        quantity = quantity
    )
}