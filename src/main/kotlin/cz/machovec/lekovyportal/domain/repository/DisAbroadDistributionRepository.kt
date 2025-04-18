package cz.machovec.lekovyportal.domain.repository

import cz.machovec.lekovyportal.domain.entity.distribution.DisAbroadDistribution
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DisAbroadDistributionRepository : JpaRepository<DisAbroadDistribution, Long>