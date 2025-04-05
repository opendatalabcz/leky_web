package cz.machovec.lekovyportal.domain.repository.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProduct

interface MpdMedicinalProductRepositoryCustom {
    fun findByFilters(atcGroupId: Long?, substanceId: Long?, query: String?): List<MpdMedicinalProduct>
}
