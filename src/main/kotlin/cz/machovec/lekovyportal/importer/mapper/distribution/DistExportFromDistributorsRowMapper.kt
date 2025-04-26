package cz.machovec.lekovyportal.importer.mapper.distribution

import cz.machovec.lekovyportal.domain.entity.distribution.DistExportFromDistributors
import cz.machovec.lekovyportal.domain.entity.distribution.DistributorExportPurchaserType
import cz.machovec.lekovyportal.domain.entity.distribution.MovementType
import cz.machovec.lekovyportal.importer.columns.distribution.DistDistributorExportCsvColumn
import cz.machovec.lekovyportal.importer.mapper.BaseRefRowMapper
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider

class DistExportFromDistributorsRowMapper(
    ref: MpdReferenceDataProvider
) : BaseRefRowMapper<DistDistributorExportCsvColumn, DistExportFromDistributors>(ref) {

    override fun map(row: CsvRow<DistDistributorExportCsvColumn>, rawLine: String): RowMappingResult<DistExportFromDistributors> {

        /* ---------- mandatory attributes ---------- */
        val period = row[DistDistributorExportCsvColumn.PERIOD]
            ?.takeIf { it.isNotBlank() }
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.MISSING_ATTRIBUTE, DistDistributorExportCsvColumn.PERIOD.name, rawLine))

        val year = period.substringBefore('.').toIntOrNull()
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.PARSE_ERROR, DistDistributorExportCsvColumn.PERIOD.name, rawLine))

        val month = period.substringAfter('.').toIntOrNull()
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.PARSE_ERROR, DistDistributorExportCsvColumn.PERIOD.name, rawLine))

        val purchaserType = row[DistDistributorExportCsvColumn.PURCHASER_TYPE]
            ?.let { DistributorExportPurchaserType.fromInput(it) }
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.UNKNOWN_REFERENCE, DistDistributorExportCsvColumn.PURCHASER_TYPE.name, rawLine))

        val suklCode = row[DistDistributorExportCsvColumn.SUKL_CODE]
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.MISSING_ATTRIBUTE, DistDistributorExportCsvColumn.SUKL_CODE.name, rawLine))

        val medicinalProduct = product(suklCode)
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.UNKNOWN_REFERENCE, DistDistributorExportCsvColumn.SUKL_CODE.name, rawLine))

        val movementType = row[DistDistributorExportCsvColumn.MOVEMENT_TYPE]
            ?.let { MovementType.fromInput(it) }
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.UNKNOWN_REFERENCE, DistDistributorExportCsvColumn.MOVEMENT_TYPE.name, rawLine))

        val packageCount = row[DistDistributorExportCsvColumn.PACKAGE_COUNT]
            ?.replace(",", ".")
            ?.toDoubleOrNull()
            ?.toInt()
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.PARSE_ERROR, DistDistributorExportCsvColumn.PACKAGE_COUNT.name, rawLine))

        val subject = row[DistDistributorExportCsvColumn.SUBJECT]
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.MISSING_ATTRIBUTE, DistDistributorExportCsvColumn.SUBJECT.name, rawLine))

        /* ---------- entity construction ---------- */
        val entity = DistExportFromDistributors(
            year = year,
            month = month,
            medicinalProduct = medicinalProduct,
            purchaserType = purchaserType,
            movementType = movementType,
            packageCount = packageCount,
            subject = subject
        )

        return RowMappingResult.Success(entity)
    }
}
