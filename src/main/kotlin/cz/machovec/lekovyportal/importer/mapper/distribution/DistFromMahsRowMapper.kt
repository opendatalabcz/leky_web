package cz.machovec.lekovyportal.importer.mapper.distribution

import cz.machovec.lekovyportal.importer.columns.distribution.DistMahCsvColumn
import cz.machovec.lekovyportal.domain.entity.distribution.DistFromMahs
import cz.machovec.lekovyportal.domain.entity.distribution.MahPurchaserType
import cz.machovec.lekovyportal.domain.entity.distribution.MovementType
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.RowMapper
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider

class DistFromMahsRowMapper(
    private val referenceDataProvider: MpdReferenceDataProvider
) : RowMapper<DistMahCsvColumn, DistFromMahs> {

    override fun map(row: CsvRow<DistMahCsvColumn>): DistFromMahs? {
        try {
            val period = row[DistMahCsvColumn.PERIOD] ?: return null
            val year = period.substringBefore('.').toIntOrNull() ?: return null
            val month = period.substringAfter('.').toIntOrNull() ?: return null

            val purchaserType = MahPurchaserType.fromInput(row[DistMahCsvColumn.PURCHASER_TYPE] ?: return null)
                ?: return null

            val suklCode = row[DistMahCsvColumn.SUKL_CODE]?.padStart(7, '0') ?: return null
            val medicinalProduct = referenceDataProvider.getMedicinalProducts()[suklCode]
                ?: return null

            val movementType = MovementType.fromInput(row[DistMahCsvColumn.MOVEMENT_TYPE] ?: return null)
                ?: return null

            val packageCount = row[DistMahCsvColumn.PACKAGE_COUNT]
                ?.replace(",", ".")
                ?.toDoubleOrNull()
                ?.toInt() ?: return null

            return DistFromMahs(
                year = year,
                month = month,
                purchaserType = purchaserType,
                medicinalProduct = medicinalProduct,
                movementType = movementType,
                packageCount = packageCount
            )
        } catch (e: Exception) {
            return null
        }
    }
}
