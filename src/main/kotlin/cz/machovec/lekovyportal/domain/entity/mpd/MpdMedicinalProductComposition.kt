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
    name = "mpd_medicinal_product_composition",
    uniqueConstraints = [UniqueConstraint(columnNames = ["medicinal_product_id", "substance_id"])]
)
data class MpdMedicinalProductComposition(
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substance_id", nullable = false)
    val substance: MpdSubstance,

    @Column(name = "sequence_number")
    val sequenceNumber: Int?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "composition_flag_id")
    val compositionFlag: MpdCompositionFlag?,

    @Column(name = "amount_from")
    val amountFrom: String?,

    @Column(name = "amount_to")
    val amountTo: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "measurement_unit_id")
    val measurementUnit: MpdMeasurementUnit?
) : BaseMpdEntity<MpdMedicinalProductComposition>() {

    override fun getUniqueKey(): String {
        return "${medicinalProduct.id}-${substance.id}"
    }

    override fun copyPreservingIdAndFirstSeen(from: MpdMedicinalProductComposition): MpdMedicinalProductComposition {
        return this.copy(
            id = from.id,
            firstSeen = from.firstSeen,
            missingSince = null
        )
    }

    override fun markMissing(since: LocalDate): MpdMedicinalProductComposition {
        return this.copy(missingSince = since)
    }

    override fun getBusinessAttributeChanges(other: MpdMedicinalProductComposition): List<AttributeChange<*>> {
        val changes = mutableListOf<AttributeChange<*>>()

        fun <T> compare(attr: String, a: T?, b: T?) {
            if (a != b) changes += AttributeChange(attr, a, b)
        }

        compare("sequenceNumber", sequenceNumber, other.sequenceNumber)
        compare("compositionFlag", compositionFlag?.id, other.compositionFlag?.id)
        compare("amountFrom", amountFrom, other.amountFrom)
        compare("amountTo", amountTo, other.amountTo)
        compare("measurementUnit", measurementUnit?.id, other.measurementUnit?.id)

        return changes
    }
}
