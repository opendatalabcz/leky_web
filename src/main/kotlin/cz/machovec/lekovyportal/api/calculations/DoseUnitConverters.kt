package cz.machovec.lekovyportal.api.calculations

import cz.machovec.lekovyportal.api.model.enums.MedicinalUnitMode
import org.springframework.stereotype.Component
import java.math.BigDecimal

interface DoseUnitConverter {
    fun convert(productId: Long, packages: BigDecimal, dddPerProduct: Map<Long, BigDecimal>): BigDecimal
}

@Component
class PackagesConverter : DoseUnitConverter {
    override fun convert(productId: Long, packages: BigDecimal, dddPerProduct: Map<Long, BigDecimal>): BigDecimal {
        return packages
    }
}

@Component
class DailyDoseConverter : DoseUnitConverter {
    override fun convert(productId: Long, packages: BigDecimal, dddPerProduct: Map<Long, BigDecimal>): BigDecimal {
        val ddd = dddPerProduct[productId] ?: BigDecimal.ZERO
        val result = packages.multiply(ddd)
        return result
    }
}

@Component
class DoseUnitConverterFactory(
    private val packages: PackagesConverter,
    private val daily: DailyDoseConverter
) {
    fun of(mode: MedicinalUnitMode): DoseUnitConverter =
        when (mode) {
            MedicinalUnitMode.PACKAGES     -> packages
            MedicinalUnitMode.DAILY_DOSES  -> daily
        }
}
