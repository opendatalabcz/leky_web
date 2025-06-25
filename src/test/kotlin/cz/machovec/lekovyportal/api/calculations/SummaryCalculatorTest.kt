package cz.machovec.lekovyportal.api.calculations

import cz.machovec.lekovyportal.core.dto.erecept.EreceptAggregatedDistrictDto
import cz.machovec.lekovyportal.core.dto.erecept.EreceptTimeSeriesDistrictDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

@DisplayName("SummaryCalculator – unit tests")
class SummaryCalculatorTest {

    private val calc = SummaryCalculator()

    private fun row(p: BigDecimal, d: BigDecimal): EreceptAggregatedDistrictDto =
        EreceptAggregatedDistrictDto(
            districtCode = "CZ0100",
            medicinalProductId = 1L,
            prescribed = p,
            dispensed = d,
            population = 100_000
        )

    @Test
    fun `correctly summarizes prescribed, dispensed and difference`() {
        val rows = listOf(
            row(BigDecimal("10.6"), BigDecimal("8.1")),
            row(BigDecimal("20.2"), BigDecimal("15.4"))
        )

        val result = calc.fromDistrictRows(rows)

        assertThat(result.prescribed).isEqualTo(31)  // 10.6 + 20.2 = 30.8 → 31
        assertThat(result.dispensed).isEqualTo(24)   // 8.1 + 15.4 = 23.5 → 24
        assertThat(result.difference).isEqualTo(7)   // 30.8 - 23.5 = 7.3 → 7
        assertThat(result.percentageDifference).isEqualTo(23.7) // (7.3 / 30.8) * 100 ≈ 23.7
    }

    @Test
    fun `returns 0 percent if prescribed is zero`() {
        val rows = listOf(row(BigDecimal.ZERO, BigDecimal("5")))

        val result = calc.fromDistrictRows(rows)

        assertThat(result.percentageDifference).isEqualTo(0.0)
    }

    @Test
    fun `calculates summary correctly for monthly rows`() {
        val rows = listOf(
            EreceptTimeSeriesDistrictDto(
                year              = 2025,
                month             = 4,
                districtCode      = "CZ0100",
                medicinalProductId= 1L,
                prescribed        = BigDecimal("100"),
                dispensed         = BigDecimal("80"),
                population        = 100_000
            )
        )

        val result = calc.fromMonthlyRows(rows)

        assertThat(result.prescribed).isEqualTo(100)
        assertThat(result.dispensed).isEqualTo(80)
        assertThat(result.difference).isEqualTo(20)
        assertThat(result.percentageDifference).isEqualTo(20.0)
    }
}
