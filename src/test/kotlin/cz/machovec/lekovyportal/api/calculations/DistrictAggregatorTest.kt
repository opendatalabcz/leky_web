package cz.machovec.lekovyportal.api.calculations

import cz.machovec.lekovyportal.api.model.enums.EreceptType
import cz.machovec.lekovyportal.api.model.enums.MedicinalUnitMode
import cz.machovec.lekovyportal.api.model.enums.NormalisationMode
import cz.machovec.lekovyportal.core.dto.erecept.EreceptAggregatedDistrictDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

@DisplayName("DistrictAggregator – unit tests")
class DistrictAggregatorTest {

    private val fakeConverterFactory = DoseUnitConverterFactory(
        packages = object : PackagesConverter() {
            override fun convert(productId: Long, packages: BigDecimal, dddMap: Map<Long, BigDecimal>): BigDecimal {
                return packages
            }
        },
        daily = object : DailyDoseConverter() {
            override fun convert(productId: Long, packages: BigDecimal, dddMap: Map<Long, BigDecimal>): BigDecimal {
                return packages
            }
        }
    )

    private val fakeIdentity = object : IdentityNormaliser() {
        override fun normalise(raw: BigDecimal, population: Int): BigDecimal = raw
    }

    private val fakePer100k = object : Per100kNormaliser() {
        override fun normalise(raw: BigDecimal, population: Int): BigDecimal = raw
    }

    private val fakeNormaliserFactory = PopulationNormaliserFactory(
        abs = fakeIdentity,
        per100k = fakePer100k
    )

    private val aggregator = DistrictAggregator(fakeConverterFactory, fakeNormaliserFactory)

    private val districtCode = "CZ0100"

    private fun row(
        productId: Long,
        prescribed: BigDecimal,
        dispensed: BigDecimal,
        population: Int
    ): EreceptAggregatedDistrictDto =
        EreceptAggregatedDistrictDto(
            districtCode, productId, prescribed, dispensed, population
        )

    @Test
    fun `aggregates PRESCRIBED values per district`() {
        val rows = listOf(
            row(1L, BigDecimal("10"), BigDecimal("8"), 100_000),
            row(2L, BigDecimal("20"), BigDecimal("15"), 100_000)
        )

        val result = aggregator.aggregate(
            rows = rows,
            aggType = EreceptType.PRESCRIBED,
            unitMode = MedicinalUnitMode.PACKAGES,
            normMode = NormalisationMode.ABSOLUTE,
            dddPerProduct = emptyMap()
        )

        assertThat(result[districtCode]).isEqualTo(30)
    }

    @Test
    fun `aggregates DISPENSED values per district`() {
        val rows = listOf(
            row(1L, BigDecimal("10"), BigDecimal("8"), 100_000),
            row(2L, BigDecimal("20"), BigDecimal("15"), 100_000)
        )

        val result = aggregator.aggregate(
            rows = rows,
            aggType = EreceptType.DISPENSED,
            unitMode = MedicinalUnitMode.PACKAGES,
            normMode = NormalisationMode.ABSOLUTE,
            dddPerProduct = emptyMap()
        )

        assertThat(result[districtCode]).isEqualTo(23)
    }

    @Test
    fun `aggregates DIFFERENCE as PRESCRIBED minus DISPENSED`() {
        val rows = listOf(
            row(1L, BigDecimal("10"), BigDecimal("8"), 100_000),
            row(2L, BigDecimal("20"), BigDecimal("15"), 100_000)
        )

        val result = aggregator.aggregate(
            rows = rows,
            aggType = EreceptType.DIFFERENCE,
            unitMode = MedicinalUnitMode.PACKAGES,
            normMode = NormalisationMode.ABSOLUTE,
            dddPerProduct = emptyMap()
        )

        assertThat(result[districtCode]).isEqualTo(7)
    }

    @Test
    fun `applies normalisation result toInt`() {
        val customNormaliser = object : Per100kNormaliser() {
            override fun normalise(raw: BigDecimal, population: Int): BigDecimal = BigDecimal("123.7")
        }

        val aggregatorWithCustomNorm = DistrictAggregator(
            converterFactory = fakeConverterFactory,
            normaliserFactory = PopulationNormaliserFactory(
                abs = fakeIdentity,
                per100k = customNormaliser
            )
        )

        val rows = listOf(row(1L, BigDecimal("10"), BigDecimal("5"), 100_000))

        val result = aggregatorWithCustomNorm.aggregate(
            rows,
            aggType = EreceptType.DIFFERENCE,
            unitMode = MedicinalUnitMode.PACKAGES,
            normMode = NormalisationMode.PER_100000_CAPITA,
            dddPerProduct = emptyMap()
        )

        assertThat(result[districtCode]).isEqualTo(124)
    }
}

