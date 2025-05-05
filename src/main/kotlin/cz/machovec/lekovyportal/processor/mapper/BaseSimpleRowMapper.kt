package cz.machovec.lekovyportal.processor.mapper

/**
 * Base mapper that only offers `safeTrim()`.
 * Use for tables that do not need reference-lookup.
 */
abstract class BaseSimpleRowMapper<E : Enum<E>, T> :
    RowMapper<E, T> {

    protected fun String?.safeTrim(): String? =
        this?.trim()?.takeIf { it.isNotBlank() }
}
