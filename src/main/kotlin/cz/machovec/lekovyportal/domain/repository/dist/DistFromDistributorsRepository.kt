package cz.machovec.lekovyportal.domain.repository.dist

import cz.machovec.lekovyportal.domain.dto.MonthlyMovementAggregate
import cz.machovec.lekovyportal.domain.entity.distribution.DistFromDistributors
import cz.machovec.lekovyportal.domain.entity.distribution.DistributorPurchaserType
import cz.machovec.lekovyportal.domain.entity.distribution.MovementType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DistFromDistributorsRepository : JpaRepository<DistFromDistributors, Long> {

    @Query("""
        SELECT 
            d.purchaserType AS purchaserType,
            d.movementType AS movementType,
            SUM(d.packageCount) AS totalCount
        FROM DistFromDistributors d
        WHERE 
            d.medicinalProduct.id IN :productIds
            AND (d.year > :fromYear OR (d.year = :fromYear AND d.month >= :fromMonth))
            AND (d.year < :toYear OR (d.year = :toYear AND d.month <= :toMonth))
        GROUP BY d.purchaserType, d.movementType
    """)
    fun sumByPurchaser(
        @Param("productIds") productIds: List<Long>,
        @Param("fromYear") fromYear: Int,
        @Param("fromMonth") fromMonth: Int,
        @Param("toYear") toYear: Int,
        @Param("toMonth") toMonth: Int
    ): List<DistributorPurchaserAggregation>

    @Query("""
        SELECT new cz.machovec.lekovyportal.domain.dto.MonthlyMovementAggregate(
            d.year, d.month, d.purchaserType, d.movementType, SUM(d.packageCount)
        )
        FROM DistFromDistributors d
        WHERE d.medicinalProduct.id IN :productIds
        GROUP BY d.year, d.month, d.purchaserType, d.movementType
    """)
    fun findMonthlyAggregates(@Param("productIds") productIds: List<Long>): List<MonthlyMovementAggregate>
}

interface DistributorPurchaserAggregation {
    val purchaserType: DistributorPurchaserType
    val movementType: MovementType
    val totalCount: Int
}
