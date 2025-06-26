package cz.machovec.lekovyportal.processor.processing

import cz.machovec.lekovyportal.processor.mapper.ColumnAlias
import cz.machovec.lekovyportal.processor.mapper.CsvRow
import cz.machovec.lekovyportal.processor.mapper.RowMapper
import cz.machovec.lekovyportal.processor.mapper.RowMappingResult
import cz.machovec.lekovyportal.processor.mapper.toSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test

@DisplayName("CsvImporter – header handling")
class CsvImporterHeaderTest {

    private val importer = CsvImporter()

    enum class DummyCol(override val aliases: List<String>, override val required: Boolean = true)
        : ColumnAlias {
        ONE(listOf("Col-1")),
        TWO(listOf("Col-2"));
    }

    // mapper that always succeeds – we only test header logic here
    private val passthroughMapper = object : RowMapper<DummyCol, String> {
        override fun map(row: CsvRow<DummyCol>, rawLine: String) =
            RowMappingResult.Success(rawLine)          // content irrelevant
    }

    @Test
    fun `throws MissingColumnException when required header absent`() {
        val csv = """
            Col-1
            value
        """.trimIndent().byteInputStream().readAllBytes()

        assertThatThrownBy {
            importer.import(
                csv,
                DummyCol.entries.map { it.toSpec() },
                passthroughMapper
            )
        }.isInstanceOf(MissingColumnException::class.java)
            .hasMessageContaining("TWO")
    }

    @Test
    fun `accepts header aliases case-insensitively`() {
        val csv = """
            col-1;COL-2
            A;B
        """.trimIndent().toByteArray()

        val result = importer.import(
            csv,
            DummyCol.entries.map { it.toSpec() },
            passthroughMapper
        )

        assertThat(result.successes).hasSize(1)
        assertThat(result.failures).isEmpty()
    }
}
