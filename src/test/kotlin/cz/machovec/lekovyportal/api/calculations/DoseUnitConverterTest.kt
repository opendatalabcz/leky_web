package cz.machovec.lekovyportal.api.calculations

import cz.machovec.lekovyportal.api.model.enums.MedicinalUnitMode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal

@DisplayName("DoseUnitConverter – unit tests")
class DoseUnitConverterTest {

    private val packagesConverter = PackagesConverter()
    private val dailyConverter    = DailyDoseConverter()
    private val factory           = DoseUnitConverterFactory(packagesConverter, dailyConverter)

    private val productIdA = 11L
    private val productIdB = 22L

    private val dddMap = mapOf(
        productIdA to BigDecimal("30"),   // package A = 30 DDD
        productIdB to BigDecimal("10")    // package B = 10 DDD
    )

    // ---------- PackagesConverter -------------------------------------------------------------

    @Nested
    @DisplayName("PackagesConverter")
    inner class PackagesConv {

        @Test
        fun `returns identical number of packages`() {
            val input = BigDecimal("5")
            val result = packagesConverter.convert(productIdA, input, dddMap)
            assertThat(result).isEqualByComparingTo(input)
        }
    }

    // ---------- DailyDoseConverter ------------------------------------------------------------

    @Nested
    @DisplayName("DailyDoseConverter")
    inner class DailyConv {

        @Test
        fun `multiplies by DDD per package`() {
            val inputPackages = BigDecimal("4")      // 4 packages
            val expected      = BigDecimal("120")    // 4 × 30 DDD
            val result        = dailyConverter.convert(productIdA, inputPackages, dddMap)

            assertThat(result).isEqualByComparingTo(expected)
        }

        @Test
        fun `returns zero if product DDD missing`() {
            val inputPackages = BigDecimal("2")
            val result        = dailyConverter.convert(99L, inputPackages, dddMap)

            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO)
        }
    }

    // ---------- Factory -----------------------------------------------------------------------

    @Nested
    @DisplayName("DoseUnitConverterFactory")
    inner class Factory {

        @Test
        fun `returns PackagesConverter for PACKAGES mode`() {
            val converter = factory.of(MedicinalUnitMode.PACKAGES)
            assertThat(converter).isSameAs(packagesConverter)
        }

        @Test
        fun `returns DailyDoseConverter for DAILY_DOSES mode`() {
            val converter = factory.of(MedicinalUnitMode.DAILY_DOSES)
            assertThat(converter).isSameAs(dailyConverter)
        }
    }
}
