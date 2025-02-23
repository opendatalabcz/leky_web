package cz.machovec.lekovyportal.domain.repository

import cz.machovec.lekovyportal.domain.entity.EreceptDispense
import cz.machovec.lekovyportal.domain.entity.EreceptDispenseId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EreceptDispenseRepository : JpaRepository<EreceptDispense, EreceptDispenseId>
