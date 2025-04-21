package cz.machovec.lekovyportal.domain.repository.dist

import cz.machovec.lekovyportal.domain.entity.distribution.DistFromPharmacies
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DistFromPharmaciesRepository : JpaRepository<DistFromPharmacies, Long>