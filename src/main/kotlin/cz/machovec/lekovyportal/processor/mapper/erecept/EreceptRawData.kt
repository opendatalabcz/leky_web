package cz.machovec.lekovyportal.processor.mapper.erecept

import cz.machovec.lekovyportal.core.domain.erecept.District
import cz.machovec.lekovyportal.core.domain.erecept.EreceptDispense
import cz.machovec.lekovyportal.core.domain.erecept.EreceptPrescription
import cz.machovec.lekovyportal.processor.processing.mpd.MpdReferenceDataProvider

data class EreceptRawData(
    val districtCode: String,
    val year: Int,
    val month: Int,
    val suklCode: String,
    val quantity: Int
)

fun EreceptRawData.toDispenseEntity(
    mpdReferenceDataProvider: MpdReferenceDataProvider,
    districtMap: Map<String, District>
): EreceptDispense? {
    val medicinalProduct = mpdReferenceDataProvider.getMedicinalProducts()[suklCode] ?: return null
    val district = districtMap[districtCode] ?: return null

    return EreceptDispense(
        district = district,
        year = year,
        month = month,
        medicinalProduct = medicinalProduct,
        quantity = quantity
    )
}

fun EreceptRawData.toPrescriptionEntity(
    mpdReferenceDataProvider: MpdReferenceDataProvider,
    districtMap: Map<String, District>
): EreceptPrescription? {
    val medicinalProduct = mpdReferenceDataProvider.getMedicinalProducts()[suklCode] ?: return null
    val district = districtMap[districtCode] ?: return null

    return EreceptPrescription(
        district = district,
        year = year,
        month = month,
        medicinalProduct = medicinalProduct,
        quantity = quantity
    )
}
