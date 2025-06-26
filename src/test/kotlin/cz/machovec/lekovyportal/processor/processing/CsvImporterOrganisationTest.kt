package cz.machovec.lekovyportal.processor.processing

import cz.machovec.lekovyportal.core.domain.mpd.MpdAddictionCategory
import cz.machovec.lekovyportal.core.domain.mpd.MpdAdministrationRoute
import cz.machovec.lekovyportal.core.domain.mpd.MpdAtcGroup
import cz.machovec.lekovyportal.core.domain.mpd.MpdCompositionFlag
import cz.machovec.lekovyportal.core.domain.mpd.MpdCountry
import cz.machovec.lekovyportal.core.domain.mpd.MpdDispenseType
import cz.machovec.lekovyportal.core.domain.mpd.MpdDopingCategory
import cz.machovec.lekovyportal.core.domain.mpd.MpdDosageForm
import cz.machovec.lekovyportal.core.domain.mpd.MpdGovernmentRegulationCategory
import cz.machovec.lekovyportal.core.domain.mpd.MpdMeasurementUnit
import cz.machovec.lekovyportal.core.domain.mpd.MpdMedicinalProduct
import cz.machovec.lekovyportal.core.domain.mpd.MpdOrganisation
import cz.machovec.lekovyportal.core.domain.mpd.MpdPackageType
import cz.machovec.lekovyportal.core.domain.mpd.MpdRegistrationProcess
import cz.machovec.lekovyportal.core.domain.mpd.MpdRegistrationStatus
import cz.machovec.lekovyportal.core.domain.mpd.MpdSource
import cz.machovec.lekovyportal.core.domain.mpd.MpdSubstance
import cz.machovec.lekovyportal.processor.mapper.FailureReason
import cz.machovec.lekovyportal.processor.mapper.mpd.MpdOrganisationColumn
import cz.machovec.lekovyportal.processor.mapper.mpd.MpdOrganisationRowMapper
import cz.machovec.lekovyportal.processor.mapper.toSpec
import cz.machovec.lekovyportal.processor.processing.mpd.MpdReferenceDataProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.mockito.Mockito.mock
import java.time.LocalDate
import kotlin.test.Test

@DisplayName("CsvImporter + MpdOrganisationRowMapper – reference lookup")
class CsvImporterOrganisationTest {

    private val importer = CsvImporter()

    private val ref = object : MpdReferenceDataProvider(
        atcGroupRepository = mock(),
        administrationRouteRepository = mock(),
        dosageFormRepository = mock(),
        packageTypeRepository = mock(),
        organisationRepository = mock(),
        registrationStatusRepository = mock(),
        registrationProcessRepository = mock(),
        dispenseTypeRepository = mock(),
        addictionCategoryRepository = mock(),
        dopingCategoryRepository = mock(),
        governmentRegulationCategoryRepository = mock(),
        indicationGroupRepository = mock(),
        measurementUnitRepository = mock(),
        countryRepository = mock(),
        sourceRepository = mock(),
        substanceRepository = mock(),
        medicinalProductRepository = mock(),
        compositionFlagRepository = mock(),
    ) {
        override fun getCountries(): Map<String, MpdCountry> {
            return mapOf(
                "CZ" to MpdCountry(
                    code = "CZ",
                    name = "Czechia",
                    nameEn = null,
                    edqmCode = null,
                    firstSeen = LocalDate.now(),
                    missingSince = null
                )
            )
        }

        override fun getOrganisations(): Map<Pair<String, String>, MpdOrganisation> = emptyMap()
        override fun getAtcGroups(): Map<String, MpdAtcGroup> = emptyMap()
        override fun getAdministrationRoutes(): Map<String, MpdAdministrationRoute> = emptyMap()
        override fun getDosageForms(): Map<String, MpdDosageForm> = emptyMap()
        override fun getPackageTypes(): Map<String, MpdPackageType> = emptyMap()
        override fun getRegistrationStatuses(): Map<String, MpdRegistrationStatus> = emptyMap()
        override fun getRegistrationProcesses(): Map<String, MpdRegistrationProcess> = emptyMap()
        override fun getMeasurementUnits(): Map<String, MpdMeasurementUnit> = emptyMap()
        override fun getDispenseTypes(): Map<String, MpdDispenseType> = emptyMap()
        override fun getAddictionCategories(): Map<String, MpdAddictionCategory> = emptyMap()
        override fun getDopingCategories(): Map<String, MpdDopingCategory> = emptyMap()
        override fun getGovRegulationCategories(): Map<String, MpdGovernmentRegulationCategory> = emptyMap()
        override fun getSources(): Map<String, MpdSource> = emptyMap()
        override fun getSubstances(): Map<String, MpdSubstance> = emptyMap()
        override fun getMedicinalProducts(): Map<String, MpdMedicinalProduct> = emptyMap()
        override fun getCompositionFlags(): Map<String, MpdCompositionFlag> = emptyMap()
    }

    @Test
    fun `fails when country code not found`() {
        val csv = """
            ZKR_ORG;ZEM;NAZEV
            ABC;XX;Unknown country
        """.trimIndent().toByteArray()

        val result = importer.import(
            csv,
            MpdOrganisationColumn.entries.map { it.toSpec() },
            MpdOrganisationRowMapper(LocalDate.now(), ref)
        )

        assertThat(result.successes).isEmpty()
        assertThat(result.failures[0].reason).isEqualTo(FailureReason.UNKNOWN_REFERENCE)
    }

    @Test
    fun `succeeds with valid reference`() {
        val csv = """
            ZKR_ORG;ZEM;NAZEV
            ABC;CZ;ACME Inc.
        """.trimIndent().toByteArray()

        val result = importer.import(
            csv,
            MpdOrganisationColumn.entries.map { it.toSpec() },
            MpdOrganisationRowMapper(LocalDate.now(), ref)
        )

        assertThat(result.successes).hasSize(1)
    }
}
