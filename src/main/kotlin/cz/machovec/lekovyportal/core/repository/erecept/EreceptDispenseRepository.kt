package cz.machovec.lekovyportal.core.repository.erecept

import cz.machovec.lekovyportal.core.domain.erecept.EreceptDispense
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EreceptDispenseRepository : JpaRepository<EreceptDispense, Long>, EreceptDispenseRepositoryCustom
