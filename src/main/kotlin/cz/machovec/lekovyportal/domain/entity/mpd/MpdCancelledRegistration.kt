package cz.machovec.lekovyportal.domain.entity.mpd

import cz.machovec.lekovyportal.domain.AttributeChange
import java.time.LocalDate
import jakarta.persistence.*

@Entity
@Table(
    name = "mpd_cancelled_registration",
    uniqueConstraints = [UniqueConstraint(columnNames = ["registration_number", "parallel_import_id"])]
)
data class MpdCancelledRegistration(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override val id: Long? = null,

    @Column(name = "first_seen", nullable = false)
    override val firstSeen: LocalDate,

    @Column(name = "missing_since")
    override val missingSince: LocalDate?,

    @Column(name = "name")
    val name: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administration_route_id")
    val administrationRoute: MpdAdministrationRoute?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dosage_form_id")
    val dosageForm: MpdDosageForm?,

    @Column(name = "strength")
    val strength: String?,

    @Column(name = "registration_number", nullable = false)
    val registrationNumber: String,

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
    val registrationStatus: MpdRegistrationStatus?
) : BaseMpdEntity<MpdCancelledRegistration>() {

    override fun getUniqueKey(): String = "${registrationNumber}-${parallelImportId ?: "null"}"

    override fun copyPreservingIdAndFirstSeen(from: MpdCancelledRegistration): MpdCancelledRegistration {
        return this.copy(
            id = from.id,
            firstSeen = from.firstSeen,
            missingSince = null
        )
    }

    override fun markMissing(since: LocalDate): MpdCancelledRegistration {
        return this.copy(missingSince = since)
    }

    override fun getBusinessAttributeChanges(other: MpdCancelledRegistration): List<AttributeChange<*>> {
        val changes = mutableListOf<AttributeChange<*>>()
        fun <T> compare(attr: String, a: T?, b: T?) {
            if (a != b) changes += AttributeChange(attr, a, b)
        }

        compare("name", name, other.name)
        compare("administrationRoute", administrationRoute?.id, other.administrationRoute?.id)
        compare("dosageForm", dosageForm?.id, other.dosageForm?.id)
        compare("strength", strength, other.strength)
        compare("parallelImportId", parallelImportId, other.parallelImportId)
        compare("mrpNumber", mrpNumber, other.mrpNumber)
        compare("registrationProcess", registrationProcess?.id, other.registrationProcess?.id)
        compare("registrationLegalBasis", registrationLegalBasis, other.registrationLegalBasis)
        compare("marketingAuthorizationHolder", marketingAuthorizationHolder?.id, other.marketingAuthorizationHolder?.id)
        compare("registrationEndDate", registrationEndDate, other.registrationEndDate)
        compare("registrationStatus", registrationStatus?.id, other.registrationStatus?.id)

        return changes
    }
}
