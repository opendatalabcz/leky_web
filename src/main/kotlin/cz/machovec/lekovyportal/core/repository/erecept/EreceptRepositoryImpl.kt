package cz.machovec.lekovyportal.core.repository.erecept

import cz.machovec.lekovyportal.core.dto.erecept.EreceptAggregatedDistrictDto
import cz.machovec.lekovyportal.core.dto.erecept.EreceptFullTimeSeriesDto
import cz.machovec.lekovyportal.core.dto.erecept.EreceptTimeSeriesDistrictDto
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.YearMonth

@Repository
class EreceptRepositoryImpl : EreceptRepository {

    @PersistenceContext
    private lateinit var em: EntityManager

    override fun getAggregatedByDistrictRows(
        medicinalProductIds: List<Long>,
        dateFrom: YearMonth?,
        dateTo: YearMonth?
    ): List<EreceptAggregatedDistrictDto> {
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
                    SELECT district_code, medicinal_product_id, SUM(quantity) AS prescribed_quantity, 0 AS dispensed_quantity
                    FROM erecept_prescription
                    WHERE medicinal_product_id IN (:medicinalProductIds)
            """.trimIndent())

            if (fromYearMonth != null && toYearMonth != null) {
                append(" AND (year * 100 + month) BETWEEN :fromYearMonth AND :toYearMonth ")
            }

            append("""
                    GROUP BY district_code, medicinal_product_id
                    UNION ALL
                    SELECT district_code, medicinal_product_id, 0 AS prescribed_quantity, SUM(quantity) AS dispensed_quantity
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
            EreceptAggregatedDistrictDto(
                districtCode = row[0] as String,
                medicinalProductId = (row[1] as Number).toLong(),
                prescribed = row[2] as BigDecimal,
                dispensed = row[3] as BigDecimal,
                population = (row[4] as Number).toInt()
            )
        }
    }

    override fun getTimeSeriesByDistrictRows(
        medicinalProductIds: List<Long>,
        dateFrom: YearMonth,
        dateTo: YearMonth
    ): List<EreceptTimeSeriesDistrictDto> {
        if (medicinalProductIds.isEmpty()) return emptyList()

        val fromYearMonth = dateFrom.year * 100 + dateFrom.monthValue
        val toYearMonth = dateTo.year * 100 + dateTo.monthValue

        val sql = """
            SELECT 
                combined.year,
                combined.month,
                combined.district_code,
                combined.medicinal_product_id,
                SUM(combined.prescribed_quantity) AS prescribed,
                SUM(combined.dispensed_quantity)  AS dispensed,
                d.population
            FROM (
                SELECT p.year, p.month, p.district_code, p.medicinal_product_id, SUM(p.quantity) AS prescribed_quantity, 0 AS dispensed_quantity
                FROM erecept_prescription p
                WHERE p.medicinal_product_id IN (:medicinalProductIds)
                  AND (p.year * 100 + p.month) BETWEEN :fromYearMonth AND :toYearMonth
                GROUP BY p.year, p.month, p.district_code, p.medicinal_product_id

                UNION ALL

                SELECT d.year, d.month, d.district_code, d.medicinal_product_id, 0 AS prescribed_quantity, SUM(d.quantity) AS dispensed_quantity
                FROM erecept_dispense d
                WHERE d.medicinal_product_id IN (:medicinalProductIds)
                  AND (d.year * 100 + d.month) BETWEEN :fromYearMonth AND :toYearMonth
                GROUP BY d.year, d.month, d.district_code, d.medicinal_product_id
            ) combined
            JOIN district d ON d.code = combined.district_code
            GROUP BY combined.year, combined.month, combined.district_code, combined.medicinal_product_id, d.population
        """.trimIndent()

        val query = em.createNativeQuery(sql)
        query.setParameter("medicinalProductIds", medicinalProductIds)
        query.setParameter("fromYearMonth", fromYearMonth)
        query.setParameter("toYearMonth", toYearMonth)

        @Suppress("UNCHECKED_CAST")
        val rows = query.resultList as List<Array<Any>>

        return rows.map { r ->
            EreceptTimeSeriesDistrictDto(
                year = (r[0] as Number).toInt(),
                month = (r[1] as Number).toInt(),
                districtCode = r[2] as String,
                medicinalProductId = (r[3] as Number).toLong(),
                prescribed = r[4] as BigDecimal,
                dispensed = r[5] as BigDecimal,
                population = (r[6] as Number).toInt()
            )
        }
    }

    override fun getFullTimeSeriesRows(medicinalProductIds: List<Long>): List<EreceptFullTimeSeriesDto> {
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
                SELECT year, month, district_code, medicinal_product_id, SUM(quantity) AS prescribed_quantity, 0 AS dispensed_quantity
                FROM erecept_prescription
                WHERE medicinal_product_id IN (:medicinalProductIds)
                GROUP BY year, month, district_code, medicinal_product_id

                UNION ALL

                SELECT year, month, district_code, medicinal_product_id, 0 AS prescribed_quantity, SUM(quantity) AS dispensed_quantity
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
            EreceptFullTimeSeriesDto(
                year = (row[0] as Number).toInt(),
                month = (row[1] as Number).toInt(),
                districtCode = row[2] as String,
                medicinalProductId = (row[3] as Number).toLong(),
                prescribed = row[4] as BigDecimal,
                dispensed = row[5] as BigDecimal
            )
        }
    }
}
