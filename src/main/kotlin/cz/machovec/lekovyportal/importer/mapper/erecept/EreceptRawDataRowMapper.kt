package cz.machovec.lekovyportal.importer.mapper.erecept

import cz.machovec.lekovyportal.importer.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult

enum class EreceptCsvColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    DISTRICT_CODE(listOf("OKRES_KOD")),
    YEAR         (listOf("ROK")),
    MONTH        (listOf("MESIC")),
    SUKL_CODE    (listOf("KOD_SUKL")),
    QUANTITY     (listOf("MNOZSTVI"));
}

class EreceptRawDataRowMapper : BaseSimpleRowMapper<EreceptCsvColumn, EreceptRawData>() {

    override fun map(row: CsvRow<EreceptCsvColumn>, rawLine: String): RowMappingResult<EreceptRawData> {

        /* ---------- mandatory attributes ---------- */
        val districtCode = row[EreceptCsvColumn.DISTRICT_CODE].safeTrim()
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.MISSING_ATTRIBUTE, EreceptCsvColumn.DISTRICT_CODE.name, rawLine))

        val year = row[EreceptCsvColumn.YEAR]?.toIntOrNull()
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.PARSE_ERROR, EreceptCsvColumn.YEAR.name, rawLine))

        val month = row[EreceptCsvColumn.MONTH]?.toIntOrNull()
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.PARSE_ERROR, EreceptCsvColumn.MONTH.name, rawLine))

        val suklCode = row[EreceptCsvColumn.SUKL_CODE].safeTrim()
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.MISSING_ATTRIBUTE, EreceptCsvColumn.SUKL_CODE.name, rawLine))

        val quantity = row[EreceptCsvColumn.QUANTITY]?.toIntOrNull()
            ?: return RowMappingResult.Failure(RowFailure(FailureReason.PARSE_ERROR, EreceptCsvColumn.QUANTITY.name, rawLine))

        /* ---------- data object construction ---------- */
        val entity = EreceptRawData(
            districtCode = districtCode,
            year = year,
            month = month,
            suklCode = suklCode,
            quantity = quantity
        )

        return RowMappingResult.Success(entity)
    }
}
