package cz.machovec.lekovyportal.core.repository.erecept

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository
import java.time.YearMonth

@Repository
class EreceptRepositoryImpl : EreceptRepository {

    @PersistenceContext
    private lateinit var em: EntityManager

    override fun findAggregatedPrescriptionDispenseByDistrict(
        medicinalProductIds: List<Long>,
        dateFrom: YearMonth?,
        dateTo: YearMonth?
    ): List<EReceptDistrictDataRow> {
        if (medicinalProductIds.isEmpty()) return emptyList()

        val fromYearMonth = dateFrom?.let { it.year * 100 + it.monthValue }
        val toYearMonth = dateTo?.let { it.year * 100 + it.monthValue }

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

            if (fromYearMonth != null && toYearMonth != null) {
                append(" AND (year * 100 + month) BETWEEN :fromYearMonth AND :toYearMonth ")
            }

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

            if (fromYearMonth != null && toYearMonth != null) {
                append(" AND (year * 100 + month) BETWEEN :fromYearMonth AND :toYearMonth ")
            }

            append("""
                GROUP BY district_code, medicinal_product_id
            ) AS c
            JOIN district d ON d.code = c.district_code
            GROUP BY d.code, c.medicinal_product_id, d.population
        """.trimIndent())
        }

        val query = em.createNativeQuery(sql)
        query.setParameter("medicinalProductIds", medicinalProductIds)
        if (fromYearMonth != null && toYearMonth != null) {
            query.setParameter("fromYearMonth", fromYearMonth)
            query.setParameter("toYearMonth", toYearMonth)
        }

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

    override fun findRawMonthlyAggregates(medicinalProductIds: List<Long>): List<EReceptRawMonthlyAggregate> {
        if (medicinalProductIds.isEmpty()) return emptyList()

        val sql = """
            SELECT 
                year,
                month,
                district_code,
                medicinal_product_id,
                SUM(prescribed_quantity) AS prescribed,
                SUM(dispensed_quantity) AS dispensed
            FROM (
                SELECT 
                    year,
                    month,
                    district_code,
                    medicinal_product_id,
                    SUM(quantity) AS prescribed_quantity,
                    0 AS dispensed_quantity
                FROM erecept_prescription
                WHERE medicinal_product_id IN (:medicinalProductIds)
                GROUP BY year, month, district_code, medicinal_product_id
    
                UNION ALL
    
                SELECT 
                    year,
                    month,
                    district_code,
                    medicinal_product_id,
                    0 AS prescribed_quantity,
                    SUM(quantity) AS dispensed_quantity
                FROM erecept_dispense
                WHERE medicinal_product_id IN (:medicinalProductIds)
                GROUP BY year, month, district_code, medicinal_product_id
            ) AS combined
            GROUP BY year, month, district_code, medicinal_product_id
        """.trimIndent()

        val query = em.createNativeQuery(sql)
        query.setParameter("medicinalProductIds", medicinalProductIds)

        @Suppress("UNCHECKED_CAST")
        val results = query.resultList as List<Array<Any>>

        return results.map { row ->
            EReceptRawMonthlyAggregate(
                year = (row[0] as Number).toInt(),
                month = (row[1] as Number).toInt(),
                districtCode = row[2] as String,
                medicinalProductId = (row[3] as Number).toLong(),
                prescribed = (row[4] as Number).toInt(),
                dispensed = (row[5] as Number).toInt()
            )
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

data class EReceptRawMonthlyAggregate(
    val year: Int,
    val month: Int,
    val districtCode: String,
    val medicinalProductId: Long,
    val prescribed: Int,
    val dispensed: Int
)
