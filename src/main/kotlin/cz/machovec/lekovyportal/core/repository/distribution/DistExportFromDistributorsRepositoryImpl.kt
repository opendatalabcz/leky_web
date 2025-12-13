package cz.machovec.lekovyportal.core.repository.distribution

import cz.machovec.lekovyportal.core.domain.distribution.DistExportFromDistributors
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class DistExportFromDistributorsRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) : DistExportFromDistributorsRepositoryCustom {

    @Transactional
    override fun batchInsert(records: List<DistExportFromDistributors>, batchSize: Int) {
        val sql = """
            INSERT INTO dist_export_from_distributors
            (year, month, medicinal_product_id, purchaser_type, movement_type, package_count, subject)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        jdbcTemplate.batchUpdate(sql, records, batchSize) { ps, record ->
            ps.setInt(1, record.year)
            ps.setInt(2, record.month)
            ps.setLong(3, record.medicinalProduct.id!!)
            ps.setString(4, record.purchaserType.name)
            ps.setString(5, record.movementType.name)
            ps.setBigDecimal(6, record.packageCount)
            ps.setString(7, record.subject)
        }
    }
}