package cz.machovec.lekovyportal.domain.repository

import cz.machovec.lekovyportal.domain.entity.EreceptDispense
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class EreceptDispenseRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) : EreceptDispenseRepositoryCustom {

    @Transactional
    override fun batchInsert(records: List<EreceptDispense>, batchSize: Int) {
        val sql = """
            INSERT INTO erecept_dispense
            (district_code, year, month, medicinal_product_id, quantity)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        jdbcTemplate.batchUpdate(sql, records, batchSize) { ps, record ->
            ps.setString(1, record.district.code)
            ps.setInt(2, record.year)
            ps.setInt(3, record.month)
            ps.setLong(4, record.medicinalProduct.id!!)
            ps.setInt(5, record.quantity)
        }
    }
}
