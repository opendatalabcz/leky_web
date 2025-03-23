package cz.machovec.lekovyportal.domain.entity.mpd

import cz.machovec.lekovyportal.domain.AttributeChange
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

@Entity
@Table(
    name = "mpd_organisation",
    uniqueConstraints = [UniqueConstraint(columnNames = ["code", "country_id"])]
)
data class MpdOrganisation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "code", nullable = false)
    val code: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    val country: MpdCountry,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "is_manufacturer", nullable = false)
    val isManufacturer: Boolean,

    @Column(name = "is_marketing_authorization_holder", nullable = false)
    val isMarketingAuthorizationHolder: Boolean,

    @Column(name = "valid_from", nullable = false)
    val firstSeen: LocalDate,

    @Column(name = "valid_to")
    val missingSince: LocalDate?,
) {
    fun getBusinessAttributeChanges(other: MpdOrganisation): List<AttributeChange<*>> {
        val changes = mutableListOf<AttributeChange<*>>()

        if (this.name != other.name)
            changes += AttributeChange("name", this.name, other.name)
        if (this.isManufacturer != other.isManufacturer)
            changes += AttributeChange("isManufacturer", this.isManufacturer, other.isManufacturer)
        if (this.isMarketingAuthorizationHolder != other.isMarketingAuthorizationHolder)
            changes += AttributeChange(
                "isMarketingAuthorizationHolder",
                this.isMarketingAuthorizationHolder,
                other.isMarketingAuthorizationHolder
            )

        return changes
    }
}
