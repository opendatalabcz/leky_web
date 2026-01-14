package cz.machovec.lekovyportal.core.repository.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdAtcGroup
import cz.machovec.lekovyportal.core.domain.mpd.MpdMedicinalProduct
import cz.machovec.lekovyportal.core.domain.mpd.MpdMedicinalProductSubstance
import cz.machovec.lekovyportal.core.domain.mpd.MpdSubstance
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class MpdMedicinalProductRepositoryImpl(
    @PersistenceContext private val em: EntityManager
) : MpdMedicinalProductRepositoryCustom {

    override fun findByFilters(
        atcGroupId: Long?,
        substanceId: Long?,
        query: String?,
        pageable: Pageable
    ): Page<MpdMedicinalProduct> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(MpdMedicinalProduct::class.java)
        val root = cq.from(MpdMedicinalProduct::class.java)

        root.fetch<MpdMedicinalProduct, MpdAtcGroup>("atcGroup", JoinType.LEFT)

        val predicates = buildPredicates(cb, root, atcGroupId, substanceId, query)
        cq.select(root).where(*predicates.toTypedArray()).distinct(true)

        val typedQuery = em.createQuery(cq)
            .setFirstResult(pageable.offset.toInt())
            .setMaxResults(pageable.pageSize)

        val resultList = typedQuery.resultList

        val countQuery = cb.createQuery(Long::class.java)
        val countRoot = countQuery.from(MpdMedicinalProduct::class.java)
        countQuery.select(cb.countDistinct(countRoot))
            .where(*buildPredicates(cb, countRoot, atcGroupId, substanceId, query).toTypedArray())

        val total = em.createQuery(countQuery).singleResult

        return PageImpl(resultList, pageable, total)
    }

    private fun buildPredicates(
        cb: CriteriaBuilder,
        root: Root<MpdMedicinalProduct>,
        atcGroupId: Long?,
        substanceId: Long?,
        query: String?
    ): List<Predicate> {
        val predicates = mutableListOf<Predicate>()

        if (atcGroupId != null) {
            val atcGroupJoin = root.join<MpdMedicinalProduct, MpdAtcGroup>("atcGroup")
            predicates += cb.equal(atcGroupJoin.get<Long>("id"), atcGroupId)
        }

        if (substanceId != null) {
            val substanceJoin = root.join<MpdMedicinalProduct, MpdMedicinalProductSubstance>("substances")
            predicates += cb.equal(substanceJoin.get<MpdSubstance>("substance").get<Long>("id"), substanceId)
        }

        if (!query.isNullOrBlank()) {
            val normalizedQuery = query.uppercase()
            val containsPattern = "%$normalizedQuery%"
            val startsWithPattern = "$normalizedQuery%"

            predicates += cb.or(
                cb.like(cb.upper(root.get("name")), containsPattern),
                cb.like(cb.upper(root.get("registrationNumber")), startsWithPattern),
                cb.like(cb.upper(root.get("suklCode")), startsWithPattern)
            )
        }
        return predicates
    }
}
