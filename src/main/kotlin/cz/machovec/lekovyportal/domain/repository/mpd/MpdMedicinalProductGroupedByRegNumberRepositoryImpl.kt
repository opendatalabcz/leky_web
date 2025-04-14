package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.api.dto.MpdMedicinalProductGroupedByRegNumberDto
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.Tuple
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class MpdMedicinalProductGroupedByRegNumberRepositoryImpl(
    @PersistenceContext private val em: EntityManager
) : MpdMedicinalProductGroupedByRegNumberRepository {

    override fun findByFilters(
        atcGroupId: Long?,
        substanceId: Long?, // TODO: zatím nepoužito
        query: String?,
        pageable: Pageable
    ): Page<MpdMedicinalProductGroupedByRegNumberDto> {
        val whereClauses = mutableListOf<String>("1=1")

        if (atcGroupId != null) {
            whereClauses += ":atcGroupId = ANY(atc_group_ids)"
        }

        if (!query.isNullOrBlank()) {
            whereClauses += """
                (
                    LOWER(registration_number) LIKE :query
                    OR EXISTS (
                        SELECT 1 FROM unnest(names) AS name WHERE LOWER(name) LIKE :query
                    )
                )
            """.trimIndent()
        }

        val whereSql = whereClauses.joinToString(" AND ")

        val selectSql = """
            SELECT 
                registration_number,
                sukl_codes,
                names,
                strengths,
                dosage_form_ids,
                administration_route_ids,
                atc_group_ids
            FROM v_medicinal_product_grouped_by_reg_number
            WHERE $whereSql
            ORDER BY registration_number
            LIMIT :limit OFFSET :offset
        """.trimIndent()

        val selectQuery = em.createNativeQuery(selectSql, Tuple::class.java)
        selectQuery.setParameter("limit", pageable.pageSize)
        selectQuery.setParameter("offset", pageable.offset)
        if (atcGroupId != null) selectQuery.setParameter("atcGroupId", atcGroupId)
        if (!query.isNullOrBlank()) selectQuery.setParameter("query", "%${query.lowercase()}%")

        val resultList = selectQuery.resultList.map { row ->
            row as Tuple
            MpdMedicinalProductGroupedByRegNumberDto(
                registrationNumber = row.get("registration_number") as String,
                suklCodes = (row.get("sukl_codes") as Array<*>).filterIsInstance<String>(),
                names = (row.get("names") as Array<*>).filterIsInstance<String>(),
                strengths = (row.get("strengths") as Array<*>).filterIsInstance<String>(),
                dosageFormIds = (row.get("dosage_form_ids") as Array<*>).filterIsInstance<Long>(),
                administrationRouteIds = (row.get("administration_route_ids") as Array<*>).filterIsInstance<Long>(),
                atcGroupIds = (row.get("atc_group_ids") as Array<*>).filterIsInstance<Long>()
            )
        }

            // Count query
        val countSql = """
            SELECT COUNT(*) 
            FROM v_medicinal_product_grouped_by_reg_number 
            WHERE $whereSql
        """.trimIndent()

        val countQuery = em.createNativeQuery(countSql)
        if (atcGroupId != null) countQuery.setParameter("atcGroupId", atcGroupId)
        if (!query.isNullOrBlank()) countQuery.setParameter("query", "%${query.lowercase()}%")
        val totalCount = (countQuery.singleResult as Number).toLong()

        return PageImpl(resultList, pageable, totalCount)
    }

    override fun findByRegistrationNumbers(regNumbers: List<String>): List<MpdMedicinalProductGroupedByRegNumberDto> {
        if (regNumbers.isEmpty()) return emptyList()

        val sql = """
            SELECT 
                registration_number,
                sukl_codes,
                names,
                strengths,
                dosage_form_ids,
                administration_route_ids,
                atc_group_ids
            FROM v_medicinal_product_grouped_by_reg_number
            WHERE registration_number = ANY(?)
            ORDER BY registration_number
        """.trimIndent()

        val results = mutableListOf<MpdMedicinalProductGroupedByRegNumberDto>()

        em.unwrap(org.hibernate.Session::class.java).doWork { connection ->
            connection.prepareStatement(sql).use { ps ->
                val array = connection.createArrayOf("text", regNumbers.toTypedArray())
                ps.setArray(1, array)

                val rs = ps.executeQuery()
                while (rs.next()) {
                    results += MpdMedicinalProductGroupedByRegNumberDto(
                        registrationNumber = rs.getString("registration_number"),
                        suklCodes = (rs.getArray("sukl_codes").array as Array<*>).filterIsInstance<String>(),
                        names = (rs.getArray("names").array as Array<*>).filterIsInstance<String>(),
                        strengths = (rs.getArray("strengths").array as Array<*>).filterIsInstance<String>(),
                        dosageFormIds = (rs.getArray("dosage_form_ids").array as Array<*>).filterIsInstance<Long>(),
                        administrationRouteIds = (rs.getArray("administration_route_ids").array as Array<*>).filterIsInstance<Long>(),
                        atcGroupIds = (rs.getArray("atc_group_ids").array as Array<*>).filterIsInstance<Long>()
                    )
                }
            }
        }

        return results
    }

}
