package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.api.model.mpd.MpdMedicinalProductGroupedByRegNumberDto
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
        substanceId: Long?,
        query: String?,
        pageable: Pageable
    ): Page<MpdMedicinalProductGroupedByRegNumberDto> {
        val whereClauses = mutableListOf("mp.registration_number IS NOT NULL AND mp.registration_number <> ''")

        if (atcGroupId != null) {
            whereClauses += "mp.atc_group_id = :atcGroupId"
        }
        if (substanceId != null) {
            whereClauses += "EXISTS (SELECT 1 FROM mpd_medicinal_product_substance mps WHERE mps.medicinal_product_id = mp.id AND mps.substance_id = :substanceId)"
        }
        if (!query.isNullOrBlank()) {
            whereClauses += "LOWER(mp.name || ' ' || mp.sukl_code || ' ' || mp.registration_number) LIKE :searchQuery"
        }

        val whereSql = whereClauses.joinToString(" AND ")

        val selectSql = """
            WITH filtered_regs AS (
                SELECT DISTINCT mp.registration_number 
                FROM mpd_medicinal_product mp
                WHERE $whereSql
                ORDER BY mp.registration_number
                LIMIT :limit OFFSET :offset
            )
            SELECT v.* FROM v_medicinal_product_grouped_by_reg_number v
            JOIN filtered_regs f ON v.registration_number = f.registration_number
            ORDER BY v.registration_number
        """.trimIndent()

        val selectQuery = em.createNativeQuery(selectSql, Tuple::class.java)
        selectQuery.setParameter("limit", pageable.pageSize)
        selectQuery.setParameter("offset", pageable.offset)

        atcGroupId?.let { selectQuery.setParameter("atcGroupId", it) }
        substanceId?.let { selectQuery.setParameter("substanceId", it) }
        if (!query.isNullOrBlank()) {
            selectQuery.setParameter("searchQuery", "%${query.lowercase()}%")
        }

        val resultList = selectQuery.resultList.map { row ->
            row as Tuple
            MpdMedicinalProductGroupedByRegNumberDto(
                registrationNumber = row.get("registration_number") as String,
                suklCodes = (row.get("sukl_codes") as Array<*>).filterIsInstance<String>(),
                names = (row.get("names") as Array<*>).filterIsInstance<String>(),
                strengths = (row.get("strengths") as Array<*>).filterIsInstance<String>(),
                dosageFormIds = (row.get("dosage_form_ids") as Array<*>).filterIsInstance<Long>(),
                administrationRouteIds = (row.get("administration_route_ids") as Array<*>).filterIsInstance<Long>(),
                atcGroupIds = (row.get("atc_group_ids") as Array<*>).filterIsInstance<Long>(),
                substanceIds = (row.get("substance_ids") as Array<*>).filterIsInstance<Long>()
            )
        }

        val countSql = """
            SELECT COUNT(DISTINCT mp.registration_number) 
            FROM mpd_medicinal_product mp
            WHERE $whereSql
        """.trimIndent()

        val countQuery = em.createNativeQuery(countSql)
        atcGroupId?.let { countQuery.setParameter("atcGroupId", it) }
        substanceId?.let { countQuery.setParameter("substanceId", it) }
        if (!query.isNullOrBlank()) {
            countQuery.setParameter("searchQuery", "%${query.lowercase()}%")
        }

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
                atc_group_ids,
                substance_ids
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
                        atcGroupIds = (rs.getArray("atc_group_ids").array as Array<*>).filterIsInstance<Long>(),
                        substanceIds = (rs.getArray("substance_ids").array as Array<*>).filterIsInstance<Long>()
                    )
                }
            }
        }

        return results
    }
}
