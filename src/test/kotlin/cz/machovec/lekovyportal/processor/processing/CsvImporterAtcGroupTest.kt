package cz.machovec.lekovyportal.processor.processing

import cz.machovec.lekovyportal.processor.mapper.FailureReason
import cz.machovec.lekovyportal.processor.mapper.mpd.MpdAtcGroupColumn
import cz.machovec.lekovyportal.processor.mapper.mpd.MpdAtcGroupRowMapper
import cz.machovec.lekovyportal.processor.mapper.toSpec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import java.time.LocalDate
import kotlin.test.Test

@DisplayName("CsvImporter + MpdAtcGroupRowMapper")
class CsvImporterAtcGroupTest {

    private val importer = CsvImporter()

    private fun csv(vararg lines: String) =
        lines.joinToString("\n").toByteArray()

    @Test
    fun `imports valid rows and skips malformed ones`() {
        val csvBytes = csv(
            "ATC;NT;NAZEV;NAZEV_EN",
            "A01AA;T;Carbo medicinalis;Medicinal charcoal",
            ";T;MISSING_CODE_ROW;"                     // <- missing mandatory CODE
        )

        val result = importer.import(
            csvBytes,
            MpdAtcGroupColumn.entries.map { it.toSpec() },
            MpdAtcGroupRowMapper(LocalDate.of(2025, 6, 1))
        )

        // one good, one bad
        assertThat(result.successes).hasSize(1)
        assertThat(result.failures).hasSize(1)
        assertThat(result.failures[0].reason).isEqualTo(FailureReason.MISSING_ATTRIBUTE)
        assertThat(result.failures[0].column).isEqualTo(MpdAtcGroupColumn.CODE.name)
    }
}
