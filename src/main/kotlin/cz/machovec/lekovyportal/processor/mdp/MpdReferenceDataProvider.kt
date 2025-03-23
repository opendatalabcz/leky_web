package cz.machovec.lekovyportal.processor.mdp

import cz.machovec.lekovyportal.domain.entity.mpd.MpdAddictionCategory
import cz.machovec.lekovyportal.domain.entity.mpd.MpdAdministrationRoute
import cz.machovec.lekovyportal.domain.entity.mpd.MpdAtcGroup
import cz.machovec.lekovyportal.domain.entity.mpd.MpdCountry
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDispenseType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDopingCategory
import cz.machovec.lekovyportal.domain.entity.mpd.MpdDosageForm
import cz.machovec.lekovyportal.domain.entity.mpd.MpdGovernmentRegulationCategory
import cz.machovec.lekovyportal.domain.entity.mpd.MpdIndicationGroup
import cz.machovec.lekovyportal.domain.entity.mpd.MpdMeasurementUnit
import cz.machovec.lekovyportal.domain.entity.mpd.MpdOrganisation
import cz.machovec.lekovyportal.domain.entity.mpd.MpdPackageType
import cz.machovec.lekovyportal.domain.entity.mpd.MpdRegistrationProcess
import cz.machovec.lekovyportal.domain.entity.mpd.MpdRegistrationStatus
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAddictionCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAdministrationRouteRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdAtcGroupRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdCountryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDispenseTypeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDopingCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdDosageFormRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdGovernmentRegulationCategoryRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdIndicationGroupRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdMeasurementUnitRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdOrganisationRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdPackageTypeRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRegistrationProcessRepository
import cz.machovec.lekovyportal.domain.repository.mpd.MpdRegistrationStatusRepository
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

@Component
class MpdReferenceDataProvider(
    private val atcGroupRepository: MpdAtcGroupRepository,
    private val administrationRouteRepository: MpdAdministrationRouteRepository,
    private val dosageFormRepository: MpdDosageFormRepository,
    private val packageTypeRepository: MpdPackageTypeRepository,
    private val organisationRepository: MpdOrganisationRepository,
    private val registrationStatusRepository: MpdRegistrationStatusRepository,
    private val registrationProcessRepository: MpdRegistrationProcessRepository,
    private val dispenseTypeRepository: MpdDispenseTypeRepository,
    private val addictionCategoryRepository: MpdAddictionCategoryRepository,
    private val dopingCategoryRepository: MpdDopingCategoryRepository,
    private val governmentRegulationCategoryRepository: MpdGovernmentRegulationCategoryRepository,
    private val indicationGroupRepository: MpdIndicationGroupRepository,
    private val measurementUnitRepository: MpdMeasurementUnitRepository,
    private val countryRepository: MpdCountryRepository,
) {

    private val cache = ConcurrentHashMap<String, Any>()

    fun getDosageForms() = Loader.loadByCode(cache, MpdDosageForm::class, dosageFormRepository::findAll) { it.code }
    fun getAdministrationRoutes() = Loader.loadByCode(cache, MpdAdministrationRoute::class, administrationRouteRepository::findAll) { it.code }
    fun getPackageTypes() = Loader.loadByCode(cache, MpdPackageType::class, packageTypeRepository::findAll) { it.code }
    fun getOrganisations() = Loader.loadByPairKey(cache, MpdOrganisation::class, organisationRepository::findAll) { it.code to it.country.code }
    fun getRegistrationStatuses() = Loader.loadByCode(cache, MpdRegistrationStatus::class, registrationStatusRepository::findAll) { it.code }
    fun getIndicationGroups() = Loader.loadByCode(cache, MpdIndicationGroup::class, indicationGroupRepository::findAll) { it.code }
    fun getAtcGroups() = Loader.loadByCode(cache, MpdAtcGroup::class, atcGroupRepository::findAll) { it.code }
    fun getRegistrationProcesses() = Loader.loadByCode(cache, MpdRegistrationProcess::class, registrationProcessRepository::findAll) { it.code }
    fun getMeasurementUnits() = Loader.loadByCode(cache, MpdMeasurementUnit::class, measurementUnitRepository::findAll) { it.code }
    fun getDispenseTypes() = Loader.loadByCode(cache, MpdDispenseType::class, dispenseTypeRepository::findAll) { it.code }
    fun getAddictionCategories() = Loader.loadByCode(cache, MpdAddictionCategory::class, addictionCategoryRepository::findAll) { it.code }
    fun getDopingCategories() = Loader.loadByCode(cache, MpdDopingCategory::class, dopingCategoryRepository::findAll) { it.code }
    fun getGovRegulationCategories() = Loader.loadByCode(cache, MpdGovernmentRegulationCategory::class, governmentRegulationCategoryRepository::findAll) { it.code }
    fun getCountries() = Loader.loadByCode(cache, MpdCountry::class, countryRepository::findAll) { it.code }

    @Suppress("UNCHECKED_CAST")
    private object Loader {
        fun <T : Any> loadByCode(
            cache: MutableMap<String, Any>,
            clazz: KClass<T>,
            loader: () -> List<T>,
            codeSelector: (T) -> String
        ): Map<String, T> {
            val key = clazz.simpleName!!
            return cache.getOrPut(key) {
                loader().associateBy(codeSelector)
            } as Map<String, T>
        }

        fun <T : Any> loadByPairKey(
            cache: MutableMap<String, Any>,
            clazz: KClass<T>,
            loader: () -> List<T>,
            keySelector: (T) -> Pair<String, String>
        ): Map<Pair<String, String>, T> {
            val key = clazz.simpleName!!
            return cache.getOrPut(key) {
                loader().associateBy(keySelector)
            } as Map<Pair<String, String>, T>
        }
    }
}
