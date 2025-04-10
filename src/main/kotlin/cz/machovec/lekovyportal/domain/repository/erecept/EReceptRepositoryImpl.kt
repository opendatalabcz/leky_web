package cz.machovec.lekovyportal.domain.repository.erecept

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository
import java.time.YearMonth

@Repository
class EReceptRepositoryImpl : EReceptRepository {

    @PersistenceContext
    private lateinit var em: EntityManager

    override fun findAggregatesByDistrict(
        medicinalProductIds: List<Long>,
        dateFrom: YearMonth?,
        dateTo: YearMonth?
    ): List<EReceptDistrictDataRow> {
        if (medicinalProductIds.isEmpty()) return emptyList()

        val fromYear = dateFrom?.year
        val fromMonth = dateFrom?.monthValue
        val toYear = dateTo?.year
        val toMonth = dateTo?.monthValue

        val sql = buildString {
            append("""
                SELECT 
                    d.code AS district_code,
                    c.medicinal_product_id,
                    SUM(c.prescribed_quantity) AS prescribed,
                    SUM(c.dispensed_quantity) AS dispensed,
                    d.population
                FROM (
                    SELECT 
                        district_code, 
                        medicinal_product_id, 
                        SUM(quantity) AS prescribed_quantity,
                        0 AS dispensed_quantity
                    FROM erecept_prescription
                    WHERE medicinal_product_id IN (:medicinalProductIds)
            """.trimIndent())

            if (fromYear != null && fromMonth != null)
                append(" AND (year > :fromYear OR (year = :fromYear AND month >= :fromMonth)) ")
            if (toYear != null && toMonth != null)
                append(" AND (year < :toYear OR (year = :toYear AND month <= :toMonth)) ")

            append("""
                    GROUP BY district_code, medicinal_product_id

                    UNION ALL

                    SELECT 
                        district_code, 
                        medicinal_product_id, 
                        0 AS prescribed_quantity,
                        SUM(quantity) AS dispensed_quantity
                    FROM erecept_dispense
                    WHERE medicinal_product_id IN (:medicinalProductIds)
            """.trimIndent())

            if (fromYear != null && fromMonth != null)
                append(" AND (year > :fromYear OR (year = :fromYear AND month >= :fromMonth)) ")
            if (toYear != null && toMonth != null)
                append(" AND (year < :toYear OR (year = :toYear AND month <= :toMonth)) ")

            append("""
                    GROUP BY district_code, medicinal_product_id
                ) AS c
                JOIN district d ON d.code = c.district_code
                GROUP BY d.code, c.medicinal_product_id, d.population
            """.trimIndent())
        }

        val query = em.createNativeQuery(sql)
        query.setParameter("medicinalProductIds", medicinalProductIds)
        fromYear?.let { query.setParameter("fromYear", it) }
        fromMonth?.let { query.setParameter("fromMonth", it) }
        toYear?.let { query.setParameter("toYear", it) }
        toMonth?.let { query.setParameter("toMonth", it) }

        @Suppress("UNCHECKED_CAST")
        val results = query.resultList as List<Array<Any>>

        return results.map { row ->
            val districtCode = row[0] as String
            val medicinalProductId = (row[1] as Number).toLong()
            val prescribed = (row[2] as Number).toInt()
            val dispensed = (row[3] as Number).toInt()
            val population = (row[4] as Number).toInt()
            EReceptDistrictDataRow(districtCode, medicinalProductId, prescribed, dispensed, population)
        }
    }
}

data class EReceptDistrictDataRow(
    val districtCode: String,
    val medicinalProductId: Long,
    val prescribed: Int,
    val dispensed: Int,
    val population: Int
)
