package cz.machovec.lekovyportal.api.calculations

import cz.machovec.lekovyportal.api.model.enums.NormalisationMode
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

interface PopulationNormaliser {
    fun normalise(raw: BigDecimal, population: Int): BigDecimal
}

@Component
class IdentityNormaliser : PopulationNormaliser {
    override fun normalise(raw: BigDecimal, population: Int): BigDecimal = raw
}

@Component
class Per100kNormaliser : PopulationNormaliser {
    override fun normalise(raw: BigDecimal, population: Int): BigDecimal {
        return if (population <= 0) {
            raw
        } else {
            raw
                .divide(BigDecimal.valueOf(population.toLong()), 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100_000))
                .setScale(2, RoundingMode.HALF_UP)
        }
    }
}

@Component
class PopulationNormaliserFactory(
    private val abs: IdentityNormaliser,
    private val per100k: Per100kNormaliser
) {
    fun of(mode: NormalisationMode): PopulationNormaliser =
        when (mode) {
            NormalisationMode.ABSOLUTE             -> abs
            NormalisationMode.PER_100000_CAPITA    -> per100k
        }
}
