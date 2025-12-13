package cz.machovec.lekovyportal.core.repository.distribution

import cz.machovec.lekovyportal.core.domain.distribution.DistFromPharmacies
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class DistFromPharmaciesRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) : DistFromPharmaciesRepositoryCustom {

    @Transactional
    override fun batchInsert(records: List<DistFromPharmacies>, batchSize: Int) {
        val sql = """
            INSERT INTO dist_from_pharmacies
            (year, month, medicinal_product_id, dispense_type, package_count)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        jdbcTemplate.batchUpdate(sql, records, batchSize) { ps, record ->
            ps.setInt(1, record.year)
            ps.setInt(2, record.month)
            ps.setLong(3, record.medicinalProduct.id!!)
            ps.setString(4, record.dispenseType.name)
            ps.setBigDecimal(5, record.packageCount)
        }
    }
}