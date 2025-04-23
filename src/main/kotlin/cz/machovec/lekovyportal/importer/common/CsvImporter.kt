package cz.machovec.lekovyportal.importer.common

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import cz.machovec.lekovyportal.importer.columns.ColumnSpec
import cz.machovec.lekovyportal.importer.mapper.RowMapper
import org.springframework.stereotype.Component
import java.nio.charset.Charset

class MissingColumnException(msg: String) : RuntimeException(msg)

@Component
class CsvImporter(
    private val charset: Charset = Charset.forName("Windows-1250"),
    private val separator: Char = ';'
) {
    // TODO: review
    fun <E, T> import(
        csvBytes: ByteArray,
        specs: List<ColumnSpec<E>>,
        rowMapper: RowMapper<E, T>
    ): List<T> where E : Enum<E> {
        val reader = CSVReaderBuilder(csvBytes.inputStream().reader(charset))
            .withCSVParser(CSVParserBuilder().withSeparator(separator).build())
            .build()
        val allLines = reader.readAll()
        if (allLines.isEmpty()) return emptyList()
        val header = allLines.first().mapIndexed { i, h -> h.trim().uppercase() to i }.toMap()
        specs.filter { it.required }.forEach { s ->
            if (s.aliases.none { it.uppercase() in header }) {
                throw MissingColumnException("Missing ${s.key}")
            }
        }
        val idxMap = specs.associate { s ->
            val idx = s.aliases.firstNotNullOfOrNull { header[it.uppercase()] }
            s.key to idx
        }
        val result = mutableListOf<T>()
        allLines.drop(1).forEach { cols ->
            val row = idxMap.mapValues { (_, idx) -> idx?.let { cols.getOrNull(it)?.trim() } }
            rowMapper.map(row)?.let(result::add)
        }
        return result
    }
}
