package cz.machovec.lekovyportal.core.repository.distribution

import cz.machovec.lekovyportal.core.domain.distribution.DistExportFromDistributors
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DistExportFromDistributorsRepository : JpaRepository<DistExportFromDistributors, Long>, DistExportFromDistributorsRepositoryCustom
