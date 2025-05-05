package cz.machovec.lekovyportal.domain.repository.dist

import cz.machovec.lekovyportal.domain.dto.MonthlyPharmacyAggregate
import cz.machovec.lekovyportal.domain.entity.distribution.DistFromPharmacies
import cz.machovec.lekovyportal.domain.entity.distribution.PharmacyDispenseType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface DistFromPharmaciesRepository : JpaRepository<DistFromPharmacies, Long> {

    @Query("""
        SELECT 
            SUM(d.packageCount)
        FROM DistFromPharmacies d
        WHERE 
            d.medicinalProduct.id IN :productIds
            AND (d.year > :fromYear OR (d.year = :fromYear AND d.month >= :fromMonth))
            AND (d.year < :toYear OR (d.year = :toYear AND d.month <= :toMonth))
    """)
    fun sumPackages(
        @Param("productIds") productIds: List<Long>,
        @Param("fromYear") fromYear: Int,
        @Param("fromMonth") fromMonth: Int,
        @Param("toYear") toYear: Int,
        @Param("toMonth") toMonth: Int
    ): BigDecimal?

    @Query("""
        SELECT 
            d.dispenseType AS dispenseType,
            SUM(d.packageCount) AS total
        FROM DistFromPharmacies d
        WHERE 
            d.medicinalProduct.id IN :productIds
            AND (d.year > :fromYear OR (d.year = :fromYear AND d.month >= :fromMonth))
            AND (d.year < :toYear OR (d.year = :toYear AND d.month <= :toMonth))
        GROUP BY d.dispenseType
    """)
    fun sumPackagesByDispenseType(
        @Param("productIds") productIds: List<Long>,
        @Param("fromYear") fromYear: Int,
        @Param("fromMonth") fromMonth: Int,
        @Param("toYear") toYear: Int,
        @Param("toMonth") toMonth: Int
    ): List<PharmacyDispenseTypeAggregation>

    @Query("""
    SELECT new cz.machovec.lekovyportal.domain.dto.MonthlyPharmacyAggregate(
        d.year, d.month, SUM(d.packageCount)
    )
    FROM DistFromPharmacies d
    WHERE d.medicinalProduct.id IN :productIds
    GROUP BY d.year, d.month
    """)
    fun findMonthlyAggregates(@Param("productIds") productIds: List<Long>): List<MonthlyPharmacyAggregate>

}

interface PharmacyDispenseTypeAggregation {
    val dispenseType: PharmacyDispenseType
    val total: BigDecimal
}
