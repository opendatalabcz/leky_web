package cz.machovec.lekovyportal.api.calculations

import cz.machovec.lekovyportal.api.model.enums.NormalisationMode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

@DisplayName("PopulationNormaliser – unit tests")
class PopulationNormaliserTest {

    private val identity = IdentityNormaliser()
    private val per100k  = Per100kNormaliser()
    private val factory  = PopulationNormaliserFactory(identity, per100k)

    // ---------- IdentityNormaliser ------------------------------------------------------------

    @Nested
    @DisplayName("IdentityNormaliser")
    inner class Identity {

        @Test
        fun `returns raw value unchanged`() {
            val raw = BigDecimal("1234.56")
            val result = identity.normalise(raw, 1000000)
            assertThat(result).isEqualByComparingTo(raw)
        }

        @Test
        fun `handles zero population by returning raw`() {
            val raw = BigDecimal("50")
            val result = identity.normalise(raw, 0)
            assertThat(result).isEqualByComparingTo(raw)
        }
    }

    // ---------- Per100kNormaliser --------------------------------------------------------------

    @Nested
    @DisplayName("Per100kNormaliser")
    inner class Per100k {

        @Test
        fun `normalises to value per 100 000 inhabitants`() {
            // raw   = 1500 packages
            // pop   = 75 000 people
            // expect = (1500 / 75 000) * 100 000 = 2000.00
            val raw         = BigDecimal("1500")
            val population  = 75000
            val expected    = BigDecimal("2000.00")

            val result = per100k.normalise(raw, population)
            assertThat(result).isEqualByComparingTo(expected)
        }

        @Test
        fun `returns raw when population is zero or negative`() {
            val raw = BigDecimal("999")
            val resultZero = per100k.normalise(raw, 0)
            val resultNeg  = per100k.normalise(raw, -10)

            assertThat(resultZero).isEqualByComparingTo(raw)
            assertThat(resultNeg).isEqualByComparingTo(raw)
        }

        @Test
        fun `result is rounded to two decimals with HALF_UP`() {
            // raw   = 1234
            // pop   = 12 345
            // result = (1234 / 12345) * 100000 = 9995.9497... → 9995.95
            val raw        = BigDecimal("1234")
            val population = 12_345
            val expected   = BigDecimal("9995.95")

            val result = per100k.normalise(raw, population)

            assertThat(result.scale()).isEqualTo(2)
            assertThat(result).isEqualByComparingTo(expected)
        }

    }

    // ---------- Factory -----------------------------------------------------------------------

    @Nested
    @DisplayName("PopulationNormaliserFactory")
    inner class Factory {

        @Test
        fun `returns IdentityNormaliser for ABSOLUTE mode`() {
            val normaliser = factory.of(NormalisationMode.ABSOLUTE)
            assertThat(normaliser).isSameAs(identity)
        }

        @Test
        fun `returns Per100kNormaliser for PER_100000_CAPITA mode`() {
            val normaliser = factory.of(NormalisationMode.PER_100000_CAPITA)
            assertThat(normaliser).isSameAs(per100k)
        }
    }
}
