package cz.machovec.lekovyportal.domain.repository.dist

import cz.machovec.lekovyportal.domain.entity.distribution.DistFromMahs
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DistFromMahsRepository : JpaRepository<DistFromMahs, Long>
