package cz.machovec.lekovyportal.processor.mapper

import cz.machovec.lekovyportal.core.domain.mpd.MpdMedicinalProduct
import cz.machovec.lekovyportal.processor.processing.mpd.MpdReferenceDataProvider

/**
 * Extends [BaseSimpleRowMapper] with helpers that need
 * [MpdReferenceDataProvider] (look-up by SUKL code etc.).
 */
abstract class BaseRefRowMapper<E : Enum<E>, T>(
    protected val ref: MpdReferenceDataProvider
) : BaseSimpleRowMapper<E, T>() {

    protected fun product(code: String?): MpdMedicinalProduct? =
        code.safeTrim()
            ?.padStart(7, '0')
            ?.let { ref.getMedicinalProducts()[it] }
}
