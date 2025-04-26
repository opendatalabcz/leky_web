package cz.machovec.lekovyportal.importer.mapper.distribution

import cz.machovec.lekovyportal.domain.entity.distribution.DistFromMahs
import cz.machovec.lekovyportal.domain.entity.distribution.MahPurchaserType
import cz.machovec.lekovyportal.domain.entity.distribution.MovementType
import cz.machovec.lekovyportal.importer.columns.distribution.DistMahCsvColumn
import cz.machovec.lekovyportal.importer.mapper.BaseRefRowMapper
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider

class DistFromMahsRowMapper(
    ref: MpdReferenceDataProvider
) : BaseRefRowMapper<DistMahCsvColumn, DistFromMahs>(ref) {

    override fun map(row: CsvRow<DistMahCsvColumn>, rawLine: String): RowMappingResult<DistFromMahs> {

        /* ---------- mandatory attributes ---------- */
        val period = row[DistMahCsvColumn.PERIOD]
            ?.takeIf { it.isNotBlank() }
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.MISSING_ATTRIBUTE, DistMahCsvColumn.PERIOD.name, rawLine))

        val year = period.substringBefore('.').toIntOrNull()
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.PARSE_ERROR, DistMahCsvColumn.PERIOD.name, rawLine))

        val month = period.substringAfter('.').toIntOrNull()
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.PARSE_ERROR, DistMahCsvColumn.PERIOD.name, rawLine))

        val purchaserType = row[DistMahCsvColumn.PURCHASER_TYPE]
            ?.let { MahPurchaserType.fromInput(it) }
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.UNKNOWN_REFERENCE, DistMahCsvColumn.PURCHASER_TYPE.name, rawLine))

        val suklCode = row[DistMahCsvColumn.SUKL_CODE]
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.MISSING_ATTRIBUTE, DistMahCsvColumn.SUKL_CODE.name, rawLine))

        val medicinalProduct = product(suklCode)
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.UNKNOWN_REFERENCE, DistMahCsvColumn.SUKL_CODE.name, rawLine))

        val movementType = row[DistMahCsvColumn.MOVEMENT_TYPE]
            ?.let { MovementType.fromInput(it) }
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.UNKNOWN_REFERENCE, DistMahCsvColumn.MOVEMENT_TYPE.name, rawLine))

        val packageCount = row[DistMahCsvColumn.PACKAGE_COUNT]
            ?.replace(",", ".")
            ?.toDoubleOrNull()
            ?.toInt()
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.PARSE_ERROR, DistMahCsvColumn.PACKAGE_COUNT.name, rawLine))

        /* ---------- entity construction ---------- */
        val entity = DistFromMahs(
            year = year,
            month = month,
            purchaserType = purchaserType,
            medicinalProduct = medicinalProduct,
            movementType = movementType,
            packageCount = packageCount
        )

        return RowMappingResult.Success(entity)
    }
}
