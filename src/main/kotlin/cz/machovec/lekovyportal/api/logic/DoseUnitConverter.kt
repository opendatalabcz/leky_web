package cz.machovec.lekovyportal.api.logic

import cz.machovec.lekovyportal.api.model.enums.MedicinalUnitMode
import java.math.BigDecimal

/**
 * Converts a raw package count to the requested medicinal unit.
 *
 * @param dddPerProduct map[productId] = number of DDD in one package
 */
class DoseUnitConverter(
    private val dddPerProduct: Map<Long, BigDecimal>
) {

    /**
     * @param productId   medicinal product ID
     * @param rawPackages original package count (can be negative for RETURNS)
     * @param mode        target unit (PACKAGES | DAILY_DOSES)
     */
    fun convert(
        productId: Long,
        rawPackages: Long,
        mode: MedicinalUnitMode
    ): Long = when (mode) {
        MedicinalUnitMode.PACKAGES    -> rawPackages
        MedicinalUnitMode.DAILY_DOSES -> {
            val ddd = dddPerProduct[productId] ?: BigDecimal.ZERO
            (BigDecimal(rawPackages) * ddd).longValueExact()
        }
    }
}

