package cz.machovec.lekovyportal.domain.repository.dist

import cz.machovec.lekovyportal.domain.entity.distribution.DistFromDistributors
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DistFromDistributorsRepository : JpaRepository<DistFromDistributors, Long>
