package cz.machovec.lekovyportal.importer.mapper.distribution

import cz.machovec.lekovyportal.domain.entity.distribution.DistExportFromDistributors
import cz.machovec.lekovyportal.domain.entity.distribution.DistributorExportPurchaserType
import cz.machovec.lekovyportal.domain.entity.distribution.MovementType
import cz.machovec.lekovyportal.importer.columns.distribution.DistDistributorExportCsvColumn
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.RowMapper
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider

class DistExportFromDistributorsRowMapper(
    private val referenceDataProvider: MpdReferenceDataProvider
) : RowMapper<DistDistributorExportCsvColumn, DistExportFromDistributors> {

    override fun map(row: CsvRow<DistDistributorExportCsvColumn>): DistExportFromDistributors? {
        try {
            val period = row[DistDistributorExportCsvColumn.PERIOD] ?: return null
            val year = period.substringBefore('.').toIntOrNull() ?: return null
            val month = period.substringAfter('.').toIntOrNull() ?: return null

            val purchaserType = DistributorExportPurchaserType.fromInput(row[DistDistributorExportCsvColumn.PURCHASER_TYPE] ?: return null)
                ?: return null

            val suklCode = row[DistDistributorExportCsvColumn.SUKL_CODE]?.padStart(7, '0') ?: return null
            val medicinalProduct = referenceDataProvider.getMedicinalProducts()[suklCode]
                ?: return null

            val movementType = MovementType.fromInput(row[DistDistributorExportCsvColumn.MOVEMENT_TYPE] ?: return null)
                ?: return null

            val packageCount = row[DistDistributorExportCsvColumn.PACKAGE_COUNT]
                ?.replace(",", ".")
                ?.toDoubleOrNull()
                ?.toInt() ?: return null

            val subject = row[DistDistributorExportCsvColumn.SUBJECT]?.trim()
                ?.takeIf { it.isNotBlank() } ?: return null

            return DistExportFromDistributors(
                year = year,
                month = month,
                medicinalProduct = medicinalProduct,
                purchaserType = purchaserType,
                movementType = movementType,
                packageCount = packageCount,
                subject = subject
            )
        } catch (e: Exception) {
            return null
        }
    }
}
