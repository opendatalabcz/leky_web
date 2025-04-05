package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdAtcGroup
import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProduct
import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProductSubstance
import cz.machovec.lekovyportal.domain.entity.mpd.MpdSubstance
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.Predicate
import org.springframework.stereotype.Repository

@Repository
class MpdMedicinalProductRepositoryImpl(
    @PersistenceContext private val em: EntityManager
) : MpdMedicinalProductRepositoryCustom {

    override fun findByFilters(atcGroupId: Long?, substanceId: Long?, query: String?): List<MpdMedicinalProduct> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(MpdMedicinalProduct::class.java)
        val root = cq.from(MpdMedicinalProduct::class.java)

        val predicates = mutableListOf<Predicate>()

        if (atcGroupId != null) {
            predicates += cb.equal(root.get<MpdAtcGroup>("atcGroup").get<Long>("id"), atcGroupId)
        }

        if (substanceId != null) {
            val substanceJoin = root.join<MpdMedicinalProduct, MpdMedicinalProductSubstance>("substances")
            predicates += cb.equal(substanceJoin.get<MpdSubstance>("substance").get<Long>("id"), substanceId)
        }

        if (!query.isNullOrBlank()) {
            val like = "%${query.lowercase()}%"
            predicates += cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("registrationNumber")), like),
                cb.like(cb.lower(root.get("suklCode")), like)
            )
        }

        cq.where(*predicates.toTypedArray())
        cq.select(root).distinct(true)

        return em.createQuery(cq).setMaxResults(100).resultList
    }
}
