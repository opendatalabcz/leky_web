package cz.machovec.lekovyportal.domain.repository

import cz.machovec.lekovyportal.domain.entity.EreceptDispense
import jakarta.persistence.EntityManager
import mu.KotlinLogging
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Repository
class EreceptDispenseRepositoryImpl(
    private val entityManager: EntityManager,
) : EreceptDispenseRepositoryCustom {

    @Transactional
    override fun batchInsert(records: List<EreceptDispense>, batchSize: Int) {
        records.chunked(batchSize).forEachIndexed { index, batch ->
            try {
                batch.forEachIndexed { i, record ->
                    entityManager.persist(record)
                    if ((i + 1) % batchSize == 0) {
                        entityManager.flush()
                        entityManager.clear()
                        logger.trace("Successfully saved batch number: ${index + 1}")
                    }
                }
                entityManager.flush()
                entityManager.clear()
            } catch (e: Exception) {
                logger.error("Error during saving batch: ${index + 1}: ${e.message}")
            }
        }
    }
}