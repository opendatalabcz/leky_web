package cz.machovec.lekovyportal.domain.entity.mpd

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "mpd_medicinal_product")
data class MpdMedicinalProduct(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "sukl_code", nullable = false, unique = true)
    val suklCode: String,

    @Column(name = "reporting_obligation", nullable = false)
    val reportingObligation: Boolean,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "strength")
    val strength: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dosage_form_id")
    val dosageForm: MpdDosageForm?,

    @Column(name = "packaging")
    val packaging: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administration_route_id")
    val administrationRoute: MpdAdministrationRoute?,

    @Column(name = "supplementary_information")
    val supplementaryInformation: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_type_id")
    val packageType: MpdPackageType?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marketing_authorization_holder_id")
    val marketingAuthorizationHolder: MpdOrganisation?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_marketing_authorization_holder_id")
    val currentMarketingAuthorizationHolder: MpdOrganisation?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_status_id")
    val registrationStatus: MpdRegistrationStatus?,

    @Column(name = "registration_valid_to")
    val registrationValidTo: LocalDate?,

    @Column(name = "registration_unlimited", nullable = false)
    val registrationUnlimited: Boolean,

    @Column(name = "market_supply_end_date")
    val marketSupplyEndDate: LocalDate?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indication_group_id")
    val indicationGroup: MpdIndicationGroup?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atc_group_id")
    val atcGroup: MpdAtcGroup?,

    @Column(name = "registration_number")
    val registrationNumber: String?,

    @Column(name = "parallel_import_id")
    val parallelImportId: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parallel_import_supplier_id")
    val parallelImportSupplier: MpdOrganisation?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_process_id")
    val registrationProcess: MpdRegistrationProcess?,

    @Column(name = "daily_dose_amount")
    val dailyDoseAmount: BigDecimal?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_dose_unit_id")
    val dailyDoseUnit: MpdMeasurementUnit?,

    @Column(name = "daily_dose_packaging")
    val dailyDosePackaging: BigDecimal?,

    @Column(name = "who_source")
    val whoSource: String?,

    @Column(name = "substance_list")
    val substanceList: String?, // TODO: Nahradit vazbou na `mpd_active_substance` nebo `mpd_substance`

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispense_type_id")
    val dispenseType: MpdDispenseType?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addiction_category_id")
    val addictionCategory: MpdAddictionCategory?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doping_category_id")
    val dopingCategory: MpdDopingCategory?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "government_regulation_category_id")
    val governmentRegulationCategory: MpdGovernmentRegulationCategory?,

    @Column(name = "deliveries_flag", nullable = false)
    val deliveriesFlag: Boolean,

    @Column(name = "ean")
    val ean: String?,

    @Column(name = "braille")
    val braille: String?,

    @Column(name = "expiry_period_duration")
    val expiryPeriodDuration: String?,

    @Column(name = "expiry_period_unit")
    val expiryPeriodUnit: String?,

    @Column(name = "registered_name")
    val registeredName: String?,

    @Column(name = "mrp_number")
    val mrpNumber: String?,

    @Column(name = "registration_legal_basis")
    val registrationLegalBasis: String?,

    @Column(name = "safety_feature", nullable = false)
    val safetyFeature: Boolean,

    @Column(name = "prescription_restriction", nullable = false)
    val prescriptionRestriction: Boolean,

    @Column(name = "medicinal_product_type")
    val medicinalProductType: String?,

    @Column(name = "valid_from", nullable = false)
    val validFrom: LocalDate,

    @Column(name = "valid_to")
    val validTo: LocalDate?
)
