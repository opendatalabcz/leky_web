package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDosageForm
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MpdDosageFormRepository : JpaRepository<MpdDosageForm, Long> {
    fun findByCode(code: String): MpdDosageForm?

    fun findAllByCodeIn(codes: Set<String>): List<MpdDosageForm>
}
