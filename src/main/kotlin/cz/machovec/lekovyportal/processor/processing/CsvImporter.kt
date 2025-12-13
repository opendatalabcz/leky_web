package cz.machovec.lekovyportal.processor.processing

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import cz.machovec.lekovyportal.processor.mapper.ColumnSpec
import cz.machovec.lekovyportal.processor.mapper.CsvRow
import cz.machovec.lekovyportal.processor.mapper.DataImportResult
import cz.machovec.lekovyportal.processor.mapper.MutableImportStats
import cz.machovec.lekovyportal.processor.mapper.RowFailure
import cz.machovec.lekovyportal.processor.mapper.RowMapper
import cz.machovec.lekovyportal.processor.mapper.RowMappingResult
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.io.Reader
import java.nio.charset.Charset

@Component
class CsvImporter {

    private val log = KotlinLogging.logger {}

    companion object {
        private const val DEFAULT_SEPARATOR: Char = ';'
        private val DEFAULT_CHARSET: Charset = Charset.forName("Windows-1250")
    }

    /**
     * Imports a CSV file and maps each data row to a domain entity.
     *
     * @param csvBytes raw CSV bytes
     * @param specs column specifications (aliases + required flag)
     * @param mapper row mapper that converts a CsvRow into a domain object
     * @param separator optional separator character (default is ';')
     *
     * @return [DataImportResult] containing successes and failures
     */
    fun <E, T> import(
        csvBytes: ByteArray,
        specs: List<ColumnSpec<E>>,
        mapper: RowMapper<E, T>,
        separator: Char = DEFAULT_SEPARATOR,
        charset: Charset = DEFAULT_CHARSET
    ): DataImportResult<T> where E : Enum<E> {

        val (dataRows, columnIndexMap) = parseCsv(csvBytes, specs, separator, charset)

        val successes = mutableListOf<T>()
        val failures = mutableListOf<RowFailure>()

        dataRows.forEach { dataRow ->
            val rawLine = dataRow.joinToString(separator.toString())

            val logicalRow: CsvRow<E> = columnIndexMap.mapValues { (_, idx) ->
                dataRow.getOrNull(idx)?.trim()
            }

            when (val result = mapper.map(logicalRow, rawLine)) {
                is RowMappingResult.Success -> successes += result.entity
                is RowMappingResult.Failure -> failures += result.failure
            }
        }

        if (failures.isNotEmpty()) {
            log.info { "CSV import completed – skipped ${failures.size}/${dataRows.size} rows due to errors." }
        }

        return DataImportResult(
            successes = successes,
            failures = failures,
            totalRows = dataRows.size
        )
    }

    /**
     * Streams a CSV file and maps each data row to a domain entity.
     *
     * @param reader character reader providing the CSV content
     * @param specs column specifications (aliases + required flag)
     * @param mapper row mapper that converts a CsvRow into a domain object
     * @param separator optional separator character (default is ';')
     * @param importStats mutable statistics collector for processed rows
     *
     * @return lazy [Sequence] of successfully mapped domain entities
     */
    fun <E, T> importStream(
        reader: Reader,
        specs: List<ColumnSpec<E>>,
        mapper: RowMapper<E, T>,
        separator: Char = DEFAULT_SEPARATOR,
        importStats: MutableImportStats
    ): Sequence<T> where E : Enum<E> {

        val csvReader = CSVReaderBuilder(reader)
            .withCSVParser(CSVParserBuilder().withSeparator(separator).build())
            .build()

        val headerRow = csvReader.readNext()
            ?: return emptySequence()

        val headerColumns = headerRow.map { it.trim() }
        validateColumns(headerColumns, specs)

        val columnIndexMap = buildIndexMap(headerColumns, specs)

        val resultSequence = sequence {
            csvReader.use { csvReader ->
                var rowNumber = 1

                for (dataRow in csvReader) {
                    rowNumber++

                    val logicalRow: CsvRow<E> = columnIndexMap.mapValues { (_, columnIndex) ->
                        dataRow.getOrNull(columnIndex)?.trim()
                    }

                    val rawLine = dataRow.joinToString(separator.toString())

                    when (val result = mapper.map(logicalRow, rawLine)) {
                        is RowMappingResult.Success -> {
                            importStats.recordSuccess()
                            yield(result.entity)
                        }

                        is RowMappingResult.Failure -> {
                            importStats.recordFailure(result.failure)
                        }
                    }
                }
            }
        }

        return resultSequence
    }

    /**
     * Parses the CSV header and builds a column index map.
     *
     * @return (list of data lines, index map from enum key to column index)
     */
    private fun <E> parseCsv(
        csvBytes: ByteArray,
        columnSpecs: List<ColumnSpec<E>>,
        separator: Char,
        charset: Charset
    ): Pair<List<Array<String>>, Map<E, Int>> where E : Enum<E> {

        val csvReader = CSVReaderBuilder(csvBytes.inputStream().reader(charset))
            .withCSVParser(CSVParserBuilder().withSeparator(separator).build())
            .build()

        val allRows = csvReader.readAll()
        if (allRows.isEmpty()) {
            return emptyList<Array<String>>() to emptyMap()
        }

        val headerRow = allRows.first()
        val headerColumns = headerRow.map { it.trim() }

        val dataRows = allRows.drop(1)

        validateColumns(headerColumns, columnSpecs)
        val columnIndexMap = buildIndexMap(headerColumns, columnSpecs)

        return Pair(dataRows, columnIndexMap)
    }

    private fun <E> validateColumns(
        headerColumns: List<String>,
        columnSpecs: List<ColumnSpec<E>>
    ) where E : Enum<E> {
        columnSpecs
            .filter { it.required }
            .forEach { spec ->
                val isPresent = spec.aliases.any { alias ->
                    headerColumns.any { it.equals(alias, ignoreCase = true) }
                }

                if (!isPresent) {
                    throw MissingColumnException(
                        "Missing required column: ${spec.key}"
                    )
                }
            }
    }

    private fun <E> buildIndexMap(
        headerColumns: List<String>,
        columnSpecs: List<ColumnSpec<E>>
    ): Map<E, Int> where E : Enum<E> {
        val columnIndexMap = columnSpecs
            .mapNotNull { spec ->
                val columnIndex = spec.aliases
                    .firstNotNullOfOrNull { alias ->
                        headerColumns
                            .indexOfFirst { it.equals(alias, ignoreCase = true) }
                            .takeIf { it >= 0 }
                    }

                columnIndex?.let { spec.key to it }
            }
            .toMap()

        return columnIndexMap
    }
}

class MissingColumnException(msg: String) : RuntimeException(msg)