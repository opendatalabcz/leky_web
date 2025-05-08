package cz.machovec.lekovyportal.api.calculations

import cz.machovec.lekovyportal.api.model.enums.NormalisationMode
import org.springframework.stereotype.Component
import kotlin.math.roundToLong

interface PopulationNormaliser {
    fun normalise(raw: Long, population: Int): Long
}

@Component
class IdentityNormaliser : PopulationNormaliser {
    override fun normalise(raw: Long, population: Int) = raw
}

@Component
class Per100kNormaliser : PopulationNormaliser {
    override fun normalise(raw: Long, population: Int): Long =
        if (population <= 0) raw else (raw / (population / 100_000.0)).roundToLong()
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
