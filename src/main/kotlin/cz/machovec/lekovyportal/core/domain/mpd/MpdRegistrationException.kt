package cz.machovec.lekovyportal.core.domain.mpd

import cz.machovec.lekovyportal.core.domain.AttributeChange
import java.time.LocalDate
import jakarta.persistence.*

@Entity
@Table(
    name = "mpd_registration_exception",
    uniqueConstraints = [UniqueConstraint(columnNames = ["medicinal_product_id", "valid_from"])]
)
data class MpdRegistrationException(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override val id: Long? = null,

    @Column(name = "first_seen", nullable = false)
    override val firstSeen: LocalDate,

    @Column(name = "missing_since")
    override val missingSince: LocalDate?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicinal_product_id", nullable = false)
    val medicinalProduct: MpdMedicinalProduct,

    @Column(name = "valid_from", nullable = false)
    val validFrom: LocalDate,

    @Column(name = "valid_to")
    val validTo: LocalDate?,

    @Column(name = "allowed_package_count")
    val allowedPackageCount: Int?,

    @Column(name = "purpose")
    val purpose: String?,

    @Column(name = "workplace")
    val workplace: String?,

    @Column(name = "distributor")
    val distributor: String?,

    @Column(name = "note")
    val note: String?,

    @Column(name = "submitter")
    val submitter: String?,

    @Column(name = "manufacturer")
    val manufacturer: String?
) : BaseMpdEntity<MpdRegistrationException>() {

    override fun getUniqueKey(): String {
        return "${medicinalProduct.id}-${validFrom}"
    }

    override fun copyPreservingIdAndFirstSeen(from: MpdRegistrationException): MpdRegistrationException {
        return this.copy(
            id = from.id,
            firstSeen = from.firstSeen,
            missingSince = null
        )
    }

    override fun markMissing(since: LocalDate): MpdRegistrationException {
        return this.copy(missingSince = since)
    }

    override fun getBusinessAttributeChanges(other: MpdRegistrationException): List<AttributeChange<*>> {
        val changes = mutableListOf<AttributeChange<*>>()
        fun <T> compare(attr: String, a: T?, b: T?) {
            if (a != b) changes += AttributeChange(attr, a, b)
        }

        compare("validTo", validTo, other.validTo)
        compare("allowedPackageCount", allowedPackageCount, other.allowedPackageCount)
        compare("purpose", purpose, other.purpose)
        compare("workplace", workplace, other.workplace)
        compare("distributor", distributor, other.distributor)
        compare("note", note, other.note)
        compare("submitter", submitter, other.submitter)
        compare("manufacturer", manufacturer, other.manufacturer)

        return changes
    }
}
