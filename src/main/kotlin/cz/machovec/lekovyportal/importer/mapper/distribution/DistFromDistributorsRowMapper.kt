package cz.machovec.lekovyportal.importer.mapper.distribution

import cz.machovec.lekovyportal.domain.entity.distribution.DistFromDistributors
import cz.machovec.lekovyportal.domain.entity.distribution.DistributorPurchaserType
import cz.machovec.lekovyportal.domain.entity.distribution.MovementType
import cz.machovec.lekovyportal.importer.columns.distribution.DistDistributorCsvColumn
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.RowMapper
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider

class DistFromDistributorsRowMapper(
    private val referenceDataProvider: MpdReferenceDataProvider
) : RowMapper<DistDistributorCsvColumn, DistFromDistributors> {

    override fun map(row: CsvRow<DistDistributorCsvColumn>): DistFromDistributors? {
        try {
            val period = row[DistDistributorCsvColumn.PERIOD] ?: return null
            val year = period.substringBefore('.').toIntOrNull() ?: return null
            val month = period.substringAfter('.').toIntOrNull() ?: return null

            val purchaserType = DistributorPurchaserType.fromInput(row[DistDistributorCsvColumn.PURCHASER_TYPE] ?: return null)
                ?: return null

            val suklCode = row[DistDistributorCsvColumn.SUKL_CODE]?.padStart(7, '0') ?: return null
            val medicinalProduct = referenceDataProvider.getMedicinalProducts()[suklCode]
                ?: return null

            val movementType = MovementType.fromInput(row[DistDistributorCsvColumn.MOVEMENT_TYPE] ?: return null)
                ?: return null

            val packageCount = row[DistDistributorCsvColumn.PACKAGE_COUNT]
                ?.replace(",", ".")
                ?.toDoubleOrNull()
                ?.toInt() ?: return null

            return DistFromDistributors(
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
