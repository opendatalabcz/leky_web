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

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "registered_name")
    val registeredName: String?,

    @Column(name = "strength")
    val strength: String?,

    @Column(name = "packaging")
    val packaging: String?,

    @Column(name = "supplement")
    val supplement: String?,

    @Column(name = "ean")
    val ean: String?,

    @Column(name = "expiry")
    val expiry: String?,

    @Column(name = "expiry_type")
    val expiryType: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atc_id", nullable = false)
    val atcGroup: MpdAtcGroup,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administration_route_id", nullable = false)
    val administrationRoute: MpdAdministrationRoute,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dosage_form_id", nullable = false)
    val dosageForm: MpdDosageForm,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_type_id", nullable = false)
    val packageType: MpdPackageType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marketing_authorization_holder_id", nullable = false)
    val marketingAuthorizationHolder: MpdOrganisation,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    val country: MpdCountry,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_status_id", nullable = false)
    val registrationStatus: MpdRegistrationStatus,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_process_id", nullable = false)
    val registrationProcess: MpdRegistrationProcess,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispense_type_id", nullable = false)
    val dispenseType: MpdDispenseType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addiction_category_id")
    val addictionCategory: MpdAddictionCategory?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doping_category_id")
    val dopingCategory: MpdDopingCategory?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "government_regulation_category_id")
    val governmentRegulationCategory: MpdGovernmentRegulationCategory?,

    @Column(name = "reporting_obligation", nullable = false)
    val reportingObligation: Boolean,

    @Column(name = "registration_number")
    val registrationNumber: String?,

    @Column(name = "parallel_import_id")
    val parallelImportId: String?,

    @Column(name = "parallel_import_supplier")
    val parallelImportSupplier: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parallel_import_country_id")
    val parallelImportCountry: MpdCountry?,

    @Column(name = "ddd_amount")
    val dddAmount: BigDecimal?,

    @Column(name = "ddd_unit")
    val dddUnit: String?,

    @Column(name = "ddd_packaging")
    val dddPackaging: BigDecimal?,

    @Column(name = "who_source")
    val whoSource: String?,

    @Column(name = "substance_list")
    val substanceList: String?,

    @Column(name = "deliveries_flag")
    val deliveriesFlag: Boolean?,

    @Column(name = "braille_marking")
    val brailleMarking: String?,

    @Column(name = "mrp_number")
    val mrpNumber: String?,

    @Column(name = "legal_basis")
    val legalBasis: String?,

    @Column(name = "safety_feature")
    val safetyFeature: Boolean?,

    @Column(name = "prescription_restriction")
    val prescriptionRestriction: Boolean?,

    @Column(name = "medicinal_product_type")
    val medicinalProductType: String?,

    @Column(name = "valid_from", nullable = false)
    val validFrom: LocalDate,

    @Column(name = "valid_to")
    val validTo: LocalDate?
)
