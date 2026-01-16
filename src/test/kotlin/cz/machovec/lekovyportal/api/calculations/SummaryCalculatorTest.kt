package cz.machovec.lekovyportal.api.calculations

import cz.machovec.lekovyportal.api.model.enums.MedicinalUnitMode
import cz.machovec.lekovyportal.core.dto.erecept.EreceptAggregatedDistrictDto
import cz.machovec.lekovyportal.core.dto.erecept.EreceptTimeSeriesDistrictDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

@DisplayName("SummaryCalculator – unit tests")
class SummaryCalculatorTest {

    private val packagesConverter = PackagesConverter()
    private val dailyDoseConverter = DailyDoseConverter()
    private val converterFactory = DoseUnitConverterFactory(packagesConverter, dailyDoseConverter)

    private val calc = SummaryCalculator(converterFactory)

    private val dddMap = mapOf(1L to BigDecimal("2.0"))

    private fun row(p: BigDecimal, d: BigDecimal): EreceptAggregatedDistrictDto =
        EreceptAggregatedDistrictDto(
            districtCode = "CZ0100",
            medicinalProductId = 1L,
            prescribed = p,
            dispensed = d,
            population = 100_000
        )

    @Test
    fun `correctly summarizes in PACKAGES mode`() {
        val rows = listOf(
            row(BigDecimal("10.6"), BigDecimal("8.1")),
            row(BigDecimal("20.2"), BigDecimal("15.4"))
        )

        val result = calc.fromDistrictRows(rows, MedicinalUnitMode.PACKAGES, emptyMap())

        assertThat(result.prescribed).isEqualTo(31)  // 30.8 -> 31
        assertThat(result.dispensed).isEqualTo(24)   // 23.5 -> 24
        assertThat(result.difference).isEqualTo(7)   // 7.3 -> 7
        assertThat(result.percentageDifference).isEqualTo(23.7)
    }

    @Test
    fun `correctly summarizes in DAILY_DOSES mode`() {
        val rows = listOf(
            row(BigDecimal("10"), BigDecimal("5")) // 10 balení, 5 vydáno
        )

        // Testujeme režim DDD (10 balení * 2.0 DDD = 20 DDD)
        val result = calc.fromDistrictRows(rows, MedicinalUnitMode.DAILY_DOSES, dddMap)

        assertThat(result.prescribed).isEqualTo(20)
        assertThat(result.dispensed).isEqualTo(10)
        assertThat(result.difference).isEqualTo(10)
        assertThat(result.percentageDifference).isEqualTo(50.0)
    }

    @Test
    fun `returns 0 percent if prescribed is zero`() {
        val rows = listOf(row(BigDecimal.ZERO, BigDecimal("5")))

        val result = calc.fromDistrictRows(rows, MedicinalUnitMode.PACKAGES, emptyMap())

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

        val result = calc.fromMonthlyRows(rows, MedicinalUnitMode.PACKAGES, emptyMap())

        assertThat(result.prescribed).isEqualTo(100)
        assertThat(result.dispensed).isEqualTo(80)
        assertThat(result.difference).isEqualTo(20)
        assertThat(result.percentageDifference).isEqualTo(20.0)
    }
}
