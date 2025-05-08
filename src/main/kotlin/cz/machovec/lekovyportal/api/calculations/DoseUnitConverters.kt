package cz.machovec.lekovyportal.api.calculations

import cz.machovec.lekovyportal.api.model.enums.MedicinalUnitMode
import org.springframework.stereotype.Component
import java.math.BigDecimal

interface DoseUnitConverter {
    fun convert(productId: Long, packages: Long, dddPerProduct: Map<Long, BigDecimal>): Long
}

@Component
class PackagesConverter : DoseUnitConverter {
    override fun convert(productId: Long, packages: Long, dddPerProduct: Map<Long, BigDecimal>) = packages
}

@Component
class DailyDoseConverter : DoseUnitConverter {
    override fun convert(productId: Long, packages: Long, dddPerProduct: Map<Long, BigDecimal>): Long {
        val ddd = dddPerProduct[productId] ?: BigDecimal.ZERO
        return (BigDecimal(packages) * ddd).longValueExact()
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
