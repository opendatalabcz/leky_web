package cz.machovec.lekovyportal.domain.repository.dist

import cz.machovec.lekovyportal.domain.entity.distribution.DistFromMahs
import cz.machovec.lekovyportal.domain.entity.distribution.MahPurchaserType
import cz.machovec.lekovyportal.domain.entity.distribution.MovementType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DistFromMahsRepository : JpaRepository<DistFromMahs, Long> {

    @Query("""
    SELECT 
        d.purchaserType AS purchaserType,
        d.movementType AS movementType,
        SUM(d.packageCount) AS totalCount
    FROM DistFromMahs d
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
    ): List<MahPurchaserAggregation>
}

interface MahPurchaserAggregation {
    val purchaserType: MahPurchaserType
    val movementType: MovementType
    val totalCount: Int
}
