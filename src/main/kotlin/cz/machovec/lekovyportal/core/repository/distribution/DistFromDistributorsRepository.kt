package cz.machovec.lekovyportal.core.repository.distribution

import cz.machovec.lekovyportal.core.domain.distribution.DistFromDistributors
import cz.machovec.lekovyportal.core.dto.distribution.AggregateDistributorProductMovementCountDto
import cz.machovec.lekovyportal.core.dto.distribution.MonthlyDistributorProductMovementCountDto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DistFromDistributorsRepository : JpaRepository<DistFromDistributors, Long>, DistFromDistributorsRepositoryCustom {

    @Query("""
        SELECT new cz.machovec.lekovyportal.core.dto.distribution.AggregateDistributorProductMovementCountDto(
            d.medicinalProduct.id, d.purchaserType, d.movementType, SUM(d.packageCount)
        )
        FROM DistFromDistributors d
        WHERE 
            d.medicinalProduct.id IN :productIds
            AND (d.year > :fromYear OR (d.year = :fromYear AND d.month >= :fromMonth))
            AND (d.year < :toYear OR (d.year = :toYear AND d.month <= :toMonth))
        GROUP BY d.medicinalProduct.id, d.purchaserType, d.movementType
    """)
    fun getAggregateDistributorProductMovementCounts(
        @Param("productIds") productIds: List<Long>,
        @Param("fromYear") fromYear: Int,
        @Param("fromMonth") fromMonth: Int,
        @Param("toYear") toYear: Int,
        @Param("toMonth") toMonth: Int
    ): List<AggregateDistributorProductMovementCountDto>

    @Query("""
        SELECT new cz.machovec.lekovyportal.core.dto.distribution.MonthlyDistributorProductMovementCountDto(
            d.year, d.month, d.medicinalProduct.id, d.purchaserType, d.movementType, SUM(d.packageCount)
        )
        FROM DistFromDistributors d
        WHERE d.medicinalProduct.id IN :productIds
        GROUP BY d.year, d.month, d.medicinalProduct.id, d.purchaserType, d.movementType
    """)
    fun getMonthlyDistributorProductMovementCounts(
        @Param("productIds") productIds: List<Long>
    ): List<MonthlyDistributorProductMovementCountDto>
}
