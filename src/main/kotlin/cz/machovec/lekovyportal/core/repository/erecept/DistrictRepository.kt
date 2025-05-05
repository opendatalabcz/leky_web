package cz.machovec.lekovyportal.core.repository.erecept

import cz.machovec.lekovyportal.core.domain.erecept.District
import org.springframework.data.jpa.repository.JpaRepository

interface DistrictRepository : JpaRepository<District, Long>