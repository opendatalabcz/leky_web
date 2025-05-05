package cz.machovec.lekovyportal.core.domain.mpd

import cz.machovec.lekovyportal.core.domain.AttributeChange
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
    override val id: Long? = null,

    @Column(name = "first_seen", nullable = false)
    override val firstSeen: LocalDate,

    @Column(name = "missing_since")
    override val missingSince: LocalDate?,

    @Column(name = "code", nullable = false)
    val code: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    val country: MpdCountry,

    @Column(name = "name")
    val name: String?,

    @Column(name = "is_manufacturer")
    val isManufacturer: Boolean?,

    @Column(name = "is_marketing_authorization_holder")
    val isMarketingAuthorizationHolder: Boolean?,
) : BaseMpdEntity<MpdOrganisation>() {

    override fun getUniqueKey(): String {
        return "$code-${country.id}"
    }

    override fun copyPreservingIdAndFirstSeen(from: MpdOrganisation): MpdOrganisation {
        return this.copy(
            id = from.id,
            firstSeen = from.firstSeen,
            missingSince = null
        )
    }

    override fun markMissing(since: LocalDate): MpdOrganisation {
        return this.copy(missingSince = since)
    }

    override fun getBusinessAttributeChanges(other: MpdOrganisation): List<AttributeChange<*>> {
        val changes = mutableListOf<AttributeChange<*>>()
        fun <T> compare(attr: String, a: T, b: T) {
            if (a != b) changes += AttributeChange(attr, a, b)
        }
        compare("name", name, other.name)
        compare("isManufacturer", isManufacturer, other.isManufacturer)
        compare("isMarketingAuthorizationHolder", isMarketingAuthorizationHolder, other.isMarketingAuthorizationHolder)
        return changes
    }
}
