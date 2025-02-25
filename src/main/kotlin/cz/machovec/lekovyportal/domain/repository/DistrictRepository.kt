package cz.machovec.lekovyportal.domain.repository

import cz.machovec.lekovyportal.domain.entity.District
import org.springframework.data.jpa.repository.JpaRepository

interface DistrictRepository : JpaRepository<District, Long>