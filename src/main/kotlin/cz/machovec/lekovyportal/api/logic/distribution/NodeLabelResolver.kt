// logic/distribution/NodeLabelResolver.kt
package cz.machovec.lekovyportal.api.logic.distribution

import cz.machovec.lekovyportal.core.domain.distribution.DistributorPurchaserType
import cz.machovec.lekovyportal.core.domain.distribution.PharmacyDispenseType
import cz.machovec.lekovyportal.core.domain.distribution.MahPurchaserType
import org.springframework.stereotype.Component

@Component
class NodeLabelResolver {

    fun nodeIdForMahPurchaser(type: MahPurchaserType): String = when (type) {
        MahPurchaserType.DISTRIBUTOR        -> "Distributor"
        MahPurchaserType.AUTHORIZED_PERSON  -> "OOV"
    }

    fun nodeLabelForMahPurchaser(type: MahPurchaserType): String = when (type) {
        MahPurchaserType.DISTRIBUTOR        -> "Distributor"
        MahPurchaserType.AUTHORIZED_PERSON  -> "Osoba oprávněná k výdeji (Lékař, Lékárna …)"
    }

    fun nodeIdForDistributorPurchaser(type: DistributorPurchaserType): String = when (type) {
        DistributorPurchaserType.DISTRIBUTOR_CR -> "Distributor"
        DistributorPurchaserType.PHARMACY       -> "Pharmacy"
        else                                    -> type.name       // zbytek necháme angl. id
    }

    fun nodeLabelForDistributorPurchaser(type: DistributorPurchaserType): String =
        when (type) {
            DistributorPurchaserType.DISTRIBUTOR_CR -> "Distributor"
            DistributorPurchaserType.PHARMACY       -> "Lékárna"
            DistributorPurchaserType.DISTRIBUTOR_EU -> "Distributor (EU)"
            DistributorPurchaserType.DISTRIBUTOR_NON_EU -> "Distributor (mimo EU)"
            DistributorPurchaserType.DOCTOR         -> "Lékař"
            DistributorPurchaserType.NUCLEAR_MEDICINE -> "Nukleární medicína"
            DistributorPurchaserType.SALES_REPRESENTATIVE -> "Reklamní vzorky"
            DistributorPurchaserType.HEALTHCARE_PROVIDER  -> "Zdravotnické zařízení"
            DistributorPurchaserType.VLP_SELLER           -> "Prodejce VLP"
            DistributorPurchaserType.TRANSFUSION_SERVICE  -> "Transfuzní služba"
            DistributorPurchaserType.VETERINARY_DOCTOR    -> "Veterinární lékař"
            DistributorPurchaserType.FOREIGN_ENTITY       -> "Zahraniční subjekt"
        }

    fun nodeIdForDispenseType(type: PharmacyDispenseType): String = when (type) {
        PharmacyDispenseType.PRESCRIPTION -> "Prescription"
        PharmacyDispenseType.REQUISITION  -> "Requisition"
        PharmacyDispenseType.OTC          -> "OTC"
    }

    fun nodeLabelForDispenseType(type: PharmacyDispenseType): String = when (type) {
        PharmacyDispenseType.PRESCRIPTION -> "Výdej na předpis"
        PharmacyDispenseType.REQUISITION  -> "Výdej na žádanku"
        PharmacyDispenseType.OTC          -> "Volný prodej"
    }
}
