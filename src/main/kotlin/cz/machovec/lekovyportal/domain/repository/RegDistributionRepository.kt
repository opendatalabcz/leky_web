package cz.machovec.lekovyportal.domain.repository

import cz.machovec.lekovyportal.domain.entity.RegDistribution
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RegDistributionRepository : JpaRepository<RegDistribution, Long>
