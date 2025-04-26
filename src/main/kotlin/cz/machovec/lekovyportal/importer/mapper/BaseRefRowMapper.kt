package cz.machovec.lekovyportal.importer.mapper

import cz.machovec.lekovyportal.domain.entity.mpd.MpdMedicinalProduct
import cz.machovec.lekovyportal.processor.mdp.MpdReferenceDataProvider

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
