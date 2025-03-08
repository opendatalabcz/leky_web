package cz.machovec.lekovyportal.domain.repository

import cz.machovec.lekovyportal.domain.entity.DisDistribution
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DisDistributionRepository : JpaRepository<DisDistribution, Long>
