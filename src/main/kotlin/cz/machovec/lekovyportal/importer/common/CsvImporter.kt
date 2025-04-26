package cz.machovec.lekovyportal.importer.common

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import cz.machovec.lekovyportal.importer.columns.ColumnSpec
import cz.machovec.lekovyportal.importer.mapper.RowMapper
import org.springframework.stereotype.Component
import java.nio.charset.Charset

class MissingColumnException(msg: String) : RuntimeException(msg)

/**
 * Generic CSV → entity importer.
 *
 *  • Supports arbitrary column order via {@link ColumnSpec} aliases.
 *  • Performs one-time header indexing; data rows are accessed in O(1).
 *  • Delegates business-level validation/mapping to a supplied {@link RowMapper}.
 *
 * @param charset   character set used by SUKL (default Windows-1250)
 * @param separator column delimiter (default ‘;’)
 */
@Component
class CsvImporter(
    private val charset: Charset = Charset.forName("Windows-1250"),
    private val separator: Char = ';'
) {

    /**
     * Parses [csvBytes] and maps each data row to an entity of type [T].
     *
     * @param specs      list of column definitions (aliases + required flag)
     * @param rowMapper  converts a logical CsvRow -> domain object
     *
     * @return list of successfully mapped entities; invalid / skipped rows are omitted
     * @throws MissingColumnException if any required column is not present in the header
     */
    fun <E, T> import(
        csvBytes: ByteArray,
        specs: List<ColumnSpec<E>>,
        rowMapper: RowMapper<E, T>
    ): List<T> where E : Enum<E> {

        /* ---------- 1. parse entire CSV into memory ---------- */
        val reader = CSVReaderBuilder(csvBytes.inputStream().reader(charset))
            .withCSVParser(CSVParserBuilder().withSeparator(separator).build())
            .build()
        val allLines = reader.readAll()
        if (allLines.isEmpty()) return emptyList()

        /* ---------- 2. build header map: COLUMN_NAME → index ---------- */
        val header = allLines.first()
            .mapIndexed { i, h -> h.trim().uppercase() to i }
            .toMap()

        /* ---------- 3. verify presence of required columns ---------- */
        specs.filter { it.required }.forEach { spec ->
            if (spec.aliases.none { it.uppercase() in header }) {
                throw MissingColumnException("Missing required column ${spec.key}")
            }
        }

        /* ---------- 4. pre-compute index map for fast access ---------- */
        val idxMap = specs.associate { spec ->
            val idx = spec.aliases.firstNotNullOfOrNull { header[it.uppercase()] }
            spec.key to idx          // idx can be null for optional columns
        }

        /* ---------- 5. iterate data rows ---------- */
        val result = mutableListOf<T>()
        allLines.drop(1).forEach { cols ->
            // Build logical row: enumKey → trimmed cell value (or null)
            val row = idxMap.mapValues { (_, idx) -> idx?.let { cols.getOrNull(it)?.trim() } }
            // Delegate business mapping; only add non-null results
            rowMapper.map(row)?.let(result::add)
        }

        return result
    }
}
