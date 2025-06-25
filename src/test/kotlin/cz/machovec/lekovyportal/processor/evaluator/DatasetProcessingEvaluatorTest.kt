package cz.machovec.lekovyportal.processor.evaluator

import cz.machovec.lekovyportal.core.domain.dataset.DatasetType
import cz.machovec.lekovyportal.core.domain.dataset.ProcessedDataset
import cz.machovec.lekovyportal.core.repository.ProcessedDatasetRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@DisplayName("DatasetProcessingEvaluator – unit tests (Mockito)")
class DatasetProcessingEvaluatorTest {

    private lateinit var repo: ProcessedDatasetRepository
    private lateinit var evaluator: DatasetProcessingEvaluator

    private val fixedClock: Clock = Clock.fixed(
        OffsetDateTime.of(2025, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant(),
        ZoneId.of("UTC")
    )

    @BeforeEach
    fun setup() {
        repo = mock(ProcessedDatasetRepository::class.java)
        evaluator = DatasetProcessingEvaluator(repo, fixedClock)
    }

    // --- Helper methods ---

    private fun stubMonthExists(type: DatasetType, year: Int, month: Int, exists: Boolean) {
        `when`(repo.existsByDatasetTypeAndYearAndMonth(type, year, month)).thenReturn(exists)
    }

    private fun stubAnyMpdExists(exists: Boolean) {
        `when`(repo.existsByDatasetType(DatasetType.MEDICINAL_PRODUCT_DATABASE)).thenReturn(exists)
    }

    private fun stubMpdMonthsForYear(year: Int, months: List<Int>) {
        val datasets = months.map {
            ProcessedDataset(datasetType = DatasetType.MEDICINAL_PRODUCT_DATABASE, year = year, month = it)
        }
        `when`(repo.findAllByDatasetTypeAndYear(DatasetType.MEDICINAL_PRODUCT_DATABASE, year)).thenReturn(datasets)
    }

    // --- Tests for monthly dataset evaluation ---

    @Test
    fun `should not allow processing if dataset already exists`() {
        stubMonthExists(DatasetType.ERECEPT_PRESCRIPTIONS, 2024, 5, true)

        val result = evaluator.canProcessMonth(DatasetType.ERECEPT_PRESCRIPTIONS, 2024, 5)

        assertThat(result).isFalse()
    }

    @Test
    fun `should always allow first MPD period`() {
        val result = evaluator.canProcessMonth(DatasetType.MEDICINAL_PRODUCT_DATABASE, 2021, 1)
        assertThat(result).isTrue()
    }

    @Test
    fun `should allow MPD month only if previous month exists`() {
        stubMonthExists(DatasetType.MEDICINAL_PRODUCT_DATABASE, 2024, 12, true)

        val result1 = evaluator.canProcessMonth(DatasetType.MEDICINAL_PRODUCT_DATABASE, 2025, 1)
        val result2 = evaluator.canProcessMonth(DatasetType.MEDICINAL_PRODUCT_DATABASE, 2025, 2)

        assertThat(result1).isTrue()
        assertThat(result2).isFalse()
    }

    @Test
    fun `should allow historical DISTRIBUTION month only if any MPD is imported`() {
        stubAnyMpdExists(false)
        val resultWithoutMpd = evaluator.canProcessMonth(DatasetType.DISTRIBUTIONS_FROM_DISTRIBUTORS, 2020, 8)
        assertThat(resultWithoutMpd).isFalse()

        stubAnyMpdExists(true)
        val resultWithMpd = evaluator.canProcessMonth(DatasetType.DISTRIBUTIONS_FROM_DISTRIBUTORS, 2020, 8)
        assertThat(resultWithMpd).isTrue()
    }

    @Test
    fun `should allow non-MPD month only if same-month MPD exists`() {
        stubMonthExists(DatasetType.MEDICINAL_PRODUCT_DATABASE, 2025, 4, true)
        val result1 = evaluator.canProcessMonth(DatasetType.DISTRIBUTIONS_FROM_PHARMACIES, 2025, 4)
        assertThat(result1).isTrue()

        stubMonthExists(DatasetType.MEDICINAL_PRODUCT_DATABASE, 2025, 5, false)
        val result2 = evaluator.canProcessMonth(DatasetType.DISTRIBUTIONS_FROM_PHARMACIES, 2025, 5)
        assertThat(result2).isFalse()
    }

    // --- Tests for yearly dataset evaluation ---

    @Test
    fun `should allow year before 2021 if MPD is imported and no months processed yet`() {
        stubAnyMpdExists(true)
        `when`(repo.findAllByDatasetTypeAndYear(DatasetType.ERECEPT_PRESCRIPTIONS, 2020)).thenReturn(emptyList())

        val result = evaluator.canProcessYear(DatasetType.ERECEPT_PRESCRIPTIONS, 2020)
        assertThat(result).isTrue()
    }

    @Test
    fun `should require full MPD coverage for given year`() {
        `when`(repo.findAllByDatasetTypeAndYear(DatasetType.ERECEPT_DISPENSES, 2024)).thenReturn(emptyList())

        // Incomplete: only Jan–Nov
        stubMpdMonthsForYear(2024, (1..11).toList())
        val result1 = evaluator.canProcessYear(DatasetType.ERECEPT_DISPENSES, 2024)
        assertThat(result1).isFalse()

        // Complete: Jan–Dec
        stubMpdMonthsForYear(2024, (1..12).toList())
        val result2 = evaluator.canProcessYear(DatasetType.ERECEPT_DISPENSES, 2024)
        assertThat(result2).isTrue()
    }
}
