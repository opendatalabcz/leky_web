package cz.machovec.lekovyportal.importer.mapper.distribution

import cz.machovec.lekovyportal.domain.entity.distribution.DistFromPharmacies
import cz.machovec.lekovyportal.domain.entity.distribution.PharmacyDispenseType
import cz.machovec.lekovyportal.importer.columns.distribution.DistPharmacyCsvColumn
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.RowMapper
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider

class DistFromPharmaciesRowMapper(
    private val referenceDataProvider: MpdReferenceDataProvider
) : RowMapper<DistPharmacyCsvColumn, DistFromPharmacies> {

    override fun map(row: CsvRow<DistPharmacyCsvColumn>): DistFromPharmacies? {
        try {
            val period = row[DistPharmacyCsvColumn.PERIOD] ?: return null
            val year = period.substringBefore('.').toIntOrNull() ?: return null
            val month = period.substringAfter('.').toIntOrNull() ?: return null

            val dispenseType = PharmacyDispenseType.fromInput(row[DistPharmacyCsvColumn.DISPENSE_TYPE] ?: return null)
                ?: return null

            val suklCode = row[DistPharmacyCsvColumn.SUKL_CODE]?.padStart(7, '0') ?: return null
            val medicinalProduct = referenceDataProvider.getMedicinalProducts()[suklCode]
                ?: return null

            val packageCount = row[DistPharmacyCsvColumn.PACKAGE_COUNT]
                ?.replace(",", ".")
                ?.toBigDecimalOrNull() ?: return null

            return DistFromPharmacies(
                year = year,
                month = month,
                medicinalProduct = medicinalProduct,
                dispenseType = dispenseType,
                packageCount = packageCount
            )
        } catch (e: Exception) {
            return null
        }
    }
}
