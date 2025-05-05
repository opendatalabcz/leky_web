package cz.machovec.lekovyportal.processor.mapper.distribution

import cz.machovec.lekovyportal.core.domain.distribution.DistFromMahs
import cz.machovec.lekovyportal.core.domain.distribution.MahPurchaserType
import cz.machovec.lekovyportal.core.domain.distribution.MovementType
import cz.machovec.lekovyportal.processor.mapper.BaseRefRowMapper
import cz.machovec.lekovyportal.processor.mapper.ColumnAlias
import cz.machovec.lekovyportal.processor.mapper.CsvRow
import cz.machovec.lekovyportal.processor.mapper.FailureReason
import cz.machovec.lekovyportal.processor.mapper.RowFailure
import cz.machovec.lekovyportal.processor.mapper.RowMappingResult
import cz.machovec.lekovyportal.processor.processing.mpd.MpdReferenceDataProvider

enum class DistMahCsvColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    PERIOD         (listOf("Období")),
    PURCHASER_TYPE (listOf("Typ hlášení")),
    SUKL_CODE      (listOf("Kód SÚKL")),
    MOVEMENT_TYPE  (listOf("Typ pohybu")),
    PACKAGE_COUNT  (listOf("Počet balení"));
}

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
