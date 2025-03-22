package cz.machovec.lekovyportal.domain.entity.mpd

import java.time.LocalDate
import jakarta.persistence.*

@Entity
@Table(name = "mpd_cancelled_registration")
data class MpdCancelledRegistration(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name", nullable = false)
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administration_route_id")
    val administrationRoute: MpdAdministrationRoute? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dosage_form_id")
    val dosageForm: MpdDosageForm?,

    @Column(name = "strength")
    val strength: String?,

    @Column(name = "registration_number")
    val registrationNumber: String?,

    @Column(name = "parallel_import_id")
    val parallelImportId: String?,

    @Column(name = "mrp_number")
    val mrpNumber: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_process_id")
    val registrationProcess: MpdRegistrationProcess?,

    @Column(name = "registration_legal_basis")
    val registrationLegalBasis: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marketing_authorization_holder_id")
    val marketingAuthorizationHolder: MpdOrganisation?,

    @Column(name = "registration_end_date")
    val registrationEndDate: LocalDate?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_status_id")
    val registrationStatus: MpdRegistrationStatus?,

    @Column(name = "valid_from", nullable = false)
    val validFrom: LocalDate,

    @Column(name = "valid_to")
    val validTo: LocalDate?
)
