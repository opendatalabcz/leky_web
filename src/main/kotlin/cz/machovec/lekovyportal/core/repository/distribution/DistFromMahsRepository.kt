package cz.machovec.lekovyportal.core.repository.distribution

import cz.machovec.lekovyportal.core.domain.distribution.DistFromMahs
import cz.machovec.lekovyportal.core.dto.distribution.AggregateMahProductMovementCountDto
import cz.machovec.lekovyportal.core.dto.distribution.MonthlyMahProductMovementCountDto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DistFromMahsRepository : JpaRepository<DistFromMahs, Long> {

    @Query("""
        SELECT new cz.machovec.lekovyportal.core.dto.distribution.AggregateMahProductMovementCountDto(
            d.medicinalProduct.id, d.purchaserType, d.movementType, SUM(d.packageCount)
        )
        FROM DistFromMahs d
        WHERE 
            d.medicinalProduct.id IN :productIds
            AND (d.year > :fromYear OR (d.year = :fromYear AND d.month >= :fromMonth))
            AND (d.year < :toYear OR (d.year = :toYear AND d.month <= :toMonth))
        GROUP BY d.medicinalProduct.id, d.purchaserType, d.movementType
    """)
    fun getAggregateProductMovementCounts(
        @Param("productIds") productIds: List<Long>,
        @Param("fromYear") fromYear: Int,
        @Param("fromMonth") fromMonth: Int,
        @Param("toYear") toYear: Int,
        @Param("toMonth") toMonth: Int
    ): List<AggregateMahProductMovementCountDto>

    @Query("""
        SELECT new cz.machovec.lekovyportal.core.dto.distribution.MonthlyMahProductMovementCountDto(
            d.year, d.month, d.medicinalProduct.id, d.purchaserType, d.movementType, SUM(d.packageCount)
        )
        FROM DistFromMahs d
        WHERE d.medicinalProduct.id IN :productIds
        GROUP BY d.year, d.month, d.medicinalProduct.id, d.purchaserType, d.movementType
    """)
    fun getMonthlyMahProductMovementCounts(
        @Param("productIds") productIds: List<Long>
    ): List<MonthlyMahProductMovementCountDto>
}
