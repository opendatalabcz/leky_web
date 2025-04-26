package cz.machovec.lekovyportal.importer.mapper.distribution

import cz.machovec.lekovyportal.domain.entity.distribution.DistFromPharmacies
import cz.machovec.lekovyportal.domain.entity.distribution.PharmacyDispenseType
import cz.machovec.lekovyportal.importer.columns.distribution.DistPharmacyCsvColumn
import cz.machovec.lekovyportal.importer.mapper.BaseRefRowMapper
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider

class DistFromPharmaciesRowMapper(
    ref: MpdReferenceDataProvider
) : BaseRefRowMapper<DistPharmacyCsvColumn, DistFromPharmacies>(ref) {

    override fun map(row: CsvRow<DistPharmacyCsvColumn>, rawLine: String): RowMappingResult<DistFromPharmacies> {

        /* ---------- mandatory attributes ---------- */
        val period = row[DistPharmacyCsvColumn.PERIOD]
            ?.takeIf { it.isNotBlank() }
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.MISSING_ATTRIBUTE, DistPharmacyCsvColumn.PERIOD.name, rawLine))

        val year = period.substringBefore('.').toIntOrNull()
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.PARSE_ERROR, DistPharmacyCsvColumn.PERIOD.name, rawLine))

        val month = period.substringAfter('.').toIntOrNull()
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.PARSE_ERROR, DistPharmacyCsvColumn.PERIOD.name, rawLine))

        val dispenseType = row[DistPharmacyCsvColumn.DISPENSE_TYPE]
            ?.let { PharmacyDispenseType.fromInput(it) }
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.UNKNOWN_REFERENCE, DistPharmacyCsvColumn.DISPENSE_TYPE.name, rawLine))

        val suklCode = row[DistPharmacyCsvColumn.SUKL_CODE]
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.MISSING_ATTRIBUTE, DistPharmacyCsvColumn.SUKL_CODE.name, rawLine))

        val medicinalProduct = product(suklCode)
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.UNKNOWN_REFERENCE, DistPharmacyCsvColumn.SUKL_CODE.name, rawLine))

        val packageCount = row[DistPharmacyCsvColumn.PACKAGE_COUNT]
            ?.replace(",", ".")
            ?.toBigDecimalOrNull()
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.PARSE_ERROR, DistPharmacyCsvColumn.PACKAGE_COUNT.name, rawLine))

        /* ---------- entity construction ---------- */
        val entity = DistFromPharmacies(
            year = year,
            month = month,
            medicinalProduct = medicinalProduct,
            dispenseType = dispenseType,
            packageCount = packageCount
        )

        return RowMappingResult.Success(entity)
    }
}
