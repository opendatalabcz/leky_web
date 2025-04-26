package cz.machovec.lekovyportal.importer.mapper.distribution

import cz.machovec.lekovyportal.domain.entity.distribution.DistFromDistributors
import cz.machovec.lekovyportal.domain.entity.distribution.DistributorPurchaserType
import cz.machovec.lekovyportal.domain.entity.distribution.MovementType
import cz.machovec.lekovyportal.importer.mapper.BaseRefRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider

enum class DistDistributorCsvColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    PERIOD         (listOf("Období")),
    PURCHASER_TYPE (listOf("Typ odběratele")),
    SUKL_CODE      (listOf("Kód SÚKL")),
    MOVEMENT_TYPE  (listOf("Typ pohybu")),
    PACKAGE_COUNT  (listOf("Počet balení/M"));
}

class DistFromDistributorsRowMapper(
    ref: MpdReferenceDataProvider
) : BaseRefRowMapper<DistDistributorCsvColumn, DistFromDistributors>(ref) {

    override fun map(row: CsvRow<DistDistributorCsvColumn>, rawLine: String): RowMappingResult<DistFromDistributors> {

        /* ---------- mandatory attributes ---------- */
        val period = row[DistDistributorCsvColumn.PERIOD]
            ?.takeIf { it.isNotBlank() }
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.MISSING_ATTRIBUTE, DistDistributorCsvColumn.PERIOD.name, rawLine))

        val year = period.substringBefore('.').toIntOrNull()
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.PARSE_ERROR, DistDistributorCsvColumn.PERIOD.name, rawLine))

        val month = period.substringAfter('.').toIntOrNull()
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.PARSE_ERROR, DistDistributorCsvColumn.PERIOD.name, rawLine))

        val purchaserType = row[DistDistributorCsvColumn.PURCHASER_TYPE]
            ?.let { DistributorPurchaserType.fromInput(it) }
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.UNKNOWN_REFERENCE, DistDistributorCsvColumn.PURCHASER_TYPE.name, rawLine))

        val suklCode = row[DistDistributorCsvColumn.SUKL_CODE]
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.MISSING_ATTRIBUTE, DistDistributorCsvColumn.SUKL_CODE.name, rawLine))

        val medicinalProduct = product(suklCode)
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.UNKNOWN_REFERENCE, DistDistributorCsvColumn.SUKL_CODE.name, rawLine))

        val movementType = row[DistDistributorCsvColumn.MOVEMENT_TYPE]
            ?.let { MovementType.fromInput(it) }
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.UNKNOWN_REFERENCE, DistDistributorCsvColumn.MOVEMENT_TYPE.name, rawLine))

        val packageCount = row[DistDistributorCsvColumn.PACKAGE_COUNT]
            ?.replace(",", ".")
            ?.toDoubleOrNull()
            ?.toInt()
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.PARSE_ERROR, DistDistributorCsvColumn.PACKAGE_COUNT.name, rawLine))

        /* ---------- entity construction ---------- */
        val entity = DistFromDistributors(
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
