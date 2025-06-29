package cz.machovec.lekovyportal.processor.processing

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import cz.machovec.lekovyportal.processor.mapper.ColumnSpec
import cz.machovec.lekovyportal.processor.mapper.CsvRow
import cz.machovec.lekovyportal.processor.mapper.DataImportResult
import cz.machovec.lekovyportal.processor.mapper.RowFailure
import cz.machovec.lekovyportal.processor.mapper.RowMapper
import cz.machovec.lekovyportal.processor.mapper.RowMappingResult
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.nio.charset.Charset

@Component
class CsvImporter {

    private val log = KotlinLogging.logger {}

    companion object {
        private const val DEFAULT_SEPARATOR: Char = ';'
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
        charset: Charset = Charset.forName("Windows-1250")
    ): DataImportResult<T> where E : Enum<E> {

        val (lines, idxMap) = parseCsv(csvBytes, specs, separator, charset)

        val successes = mutableListOf<T>()
        val failures = mutableListOf<RowFailure>()

        lines.forEach { lineArr ->
            val rawLine = lineArr.joinToString(separator.toString())

            val logicalRow: CsvRow<E> = idxMap.mapValues { (_, idx) ->
                lineArr.getOrNull(idx)?.trim()
            }

            when (val result = mapper.map(logicalRow, rawLine)) {
                is RowMappingResult.Success -> successes += result.entity
                is RowMappingResult.Failure -> failures += result.failure
            }
        }

        if (failures.isNotEmpty()) {
            log.info { "CSV import completed – skipped ${failures.size}/${lines.size} rows due to errors." }
        }

        return DataImportResult(
            successes = successes,
            failures = failures,
            totalRows = lines.size
        )
    }

    /**
     * Parses the CSV header and builds a column index map.
     *
     * @return (list of data lines, index map from enum key to column index)
     */
    private fun <E> parseCsv(
        csvBytes: ByteArray,
        specs: List<ColumnSpec<E>>,
        separator: Char,
        charset: Charset
    ): Pair<List<Array<String>>, Map<E, Int>> where E : Enum<E> {

        val reader = CSVReaderBuilder(csvBytes.inputStream().reader(charset))
            .withCSVParser(CSVParserBuilder().withSeparator(separator).build())
            .build()

        val allLines = reader.readAll()
        if (allLines.isEmpty()) {
            return Pair(emptyList(), emptyMap())
        }

        val header = allLines.first().map { it.trim() }
        val lines = allLines.drop(1)

        // Validate presence of required columns
        specs.filter { it.required }.forEach { spec ->
            val found = spec.aliases.any { alias ->
                header.any { it.equals(alias, ignoreCase = true) }
            }
            if (!found) {
                throw MissingColumnException("Missing required column: ${spec.key}")
            }
        }

        // Build index map for available columns
        val idxMap = specs.mapNotNull { spec ->
            val idx = spec.aliases
                .firstNotNullOfOrNull { alias ->
                    header.indexOfFirst { it.equals(alias, ignoreCase = true) }
                        .takeIf { it >= 0 }
                }
            idx?.let { spec.key to it }
        }.toMap()

        return Pair(lines, idxMap)
    }
}

class MissingColumnException(msg: String) : RuntimeException(msg)