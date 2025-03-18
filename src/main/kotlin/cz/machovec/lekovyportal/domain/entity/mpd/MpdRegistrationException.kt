package cz.machovec.lekovyportal.domain.entity.mpd

import java.time.LocalDate
import jakarta.persistence.*

@Entity
@Table(name = "mpd_registration_exception")
data class MpdRegistrationException(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

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
)
