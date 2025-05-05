package cz.machovec.lekovyportal.importer.mapper.erecept

import cz.machovec.lekovyportal.domain.entity.District
import cz.machovec.lekovyportal.domain.entity.EreceptDispense
import cz.machovec.lekovyportal.domain.entity.EreceptPrescription
import cz.machovec.lekovyportal.importer.processing.mpd.MpdReferenceDataProvider

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
