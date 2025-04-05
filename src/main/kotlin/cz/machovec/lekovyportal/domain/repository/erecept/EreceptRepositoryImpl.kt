package cz.machovec.lekovyportal.domain.repository.erecept

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository

@Repository
class EreceptRepositoryImpl : EreceptRepository {

    @PersistenceContext
    private lateinit var em: EntityManager

    override fun findAggregatesByDistrict(productIds: List<Long>): List<DistrictAggregateRow> {
        if (productIds.isEmpty()) return emptyList()

        val query = em.createNativeQuery("""
            SELECT 
                district_code,
                SUM(prescribed_quantity) AS prescribed,
                SUM(dispensed_quantity) AS dispensed
            FROM (
                SELECT 
                    district_code, 
                    medicinal_product_id, 
                    SUM(quantity) AS prescribed_quantity,
                    0 AS dispensed_quantity
                FROM erecept_prescription
                WHERE medicinal_product_id IN (:productIds)
                GROUP BY district_code, medicinal_product_id

                UNION ALL

                SELECT 
                    district_code, 
                    medicinal_product_id, 
                    0 AS prescribed_quantity,
                    SUM(quantity) AS dispensed_quantity
                FROM erecept_dispense
                WHERE medicinal_product_id IN (:productIds)
                GROUP BY district_code, medicinal_product_id
            ) AS combined
            GROUP BY district_code
        """.trimIndent())

        query.setParameter("productIds", productIds)

        @Suppress("UNCHECKED_CAST")
        val results = query.resultList as List<Array<Any>>

        return results.map { row ->
            val code = row[0] as String
            val prescribed = (row[1] as Number).toInt()
            val dispensed = (row[2] as Number).toInt()
            DistrictAggregateRow(code, prescribed, dispensed)
        }
    }
}

data class DistrictAggregateRow(
    val districtCode: String,
    val prescribed: Int,
    val dispensed: Int
)
