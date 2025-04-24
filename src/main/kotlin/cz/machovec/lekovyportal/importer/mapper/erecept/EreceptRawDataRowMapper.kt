package cz.machovec.lekovyportal.importer.mapper.erecept

import cz.machovec.lekovyportal.importer.columns.erecept.EreceptCsvColumn
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.RowMapper

class EreceptRawDataRowMapper : RowMapper<EreceptCsvColumn, EreceptRawData> {

    override fun map(row: CsvRow<EreceptCsvColumn>): EreceptRawData? {
        val districtCode = row[EreceptCsvColumn.DISTRICT_CODE]
            ?.trim()
            ?.takeIf { it.isNotBlank() } ?: return null

        val year = row[EreceptCsvColumn.YEAR]
            ?.toIntOrNull() ?: return null

        val month = row[EreceptCsvColumn.MONTH]
            ?.toIntOrNull() ?: return null

        val suklCode = row[EreceptCsvColumn.SUKL_CODE]
            ?.trim()
            ?.takeIf { it.isNotBlank() } ?: return null

        val quantity = row[EreceptCsvColumn.QUANTITY]
            ?.toIntOrNull() ?: return null

        return EreceptRawData(
            districtCode = districtCode,
            year = year,
            month = month,
            suklCode = suklCode,
            quantity = quantity
        )
    }
}