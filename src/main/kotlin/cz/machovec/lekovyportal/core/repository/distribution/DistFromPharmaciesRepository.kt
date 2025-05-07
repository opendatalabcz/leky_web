package cz.machovec.lekovyportal.core.repository.distribution

import cz.machovec.lekovyportal.core.domain.distribution.DistFromPharmacies
import cz.machovec.lekovyportal.core.dto.distribution.AggregatePharmacyProductDispenseCountDto
import cz.machovec.lekovyportal.core.dto.distribution.MonthlyPharmacyProductDispenseCountDto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DistFromPharmaciesRepository : JpaRepository<DistFromPharmacies, Long> {

    @Query("""
        SELECT new cz.machovec.lekovyportal.core.dto.distribution.AggregatePharmacyProductDispenseCountDto(
            d.medicinalProduct.id, d.dispenseType, SUM(d.packageCount)
        )
        FROM DistFromPharmacies d
        WHERE 
            d.medicinalProduct.id IN :productIds
            AND (d.year > :fromYear OR (d.year = :fromYear AND d.month >= :fromMonth))
            AND (d.year < :toYear OR (d.year = :toYear AND d.month <= :toMonth))
        GROUP BY d.medicinalProduct.id, d.dispenseType
    """)
    fun getAggregatePharmacyProductDispenseCounts(
        @Param("productIds") productIds: List<Long>,
        @Param("fromYear") fromYear: Int,
        @Param("fromMonth") fromMonth: Int,
        @Param("toYear") toYear: Int,
        @Param("toMonth") toMonth: Int
    ): List<AggregatePharmacyProductDispenseCountDto>

    @Query("""
        SELECT new cz.machovec.lekovyportal.core.dto.distribution.MonthlyPharmacyProductDispenseCountDto(
            d.year, d.month, d.medicinalProduct.id, d.dispenseType, SUM(d.packageCount)
        )
        FROM DistFromPharmacies d
        WHERE d.medicinalProduct.id IN :productIds
        GROUP BY d.year, d.month, d.medicinalProduct.id, d.dispenseType
    """)
    fun getMonthlyPharmacyProductDispenseCounts(
        @Param("productIds") productIds: List<Long>
    ): List<MonthlyPharmacyProductDispenseCountDto>
}

