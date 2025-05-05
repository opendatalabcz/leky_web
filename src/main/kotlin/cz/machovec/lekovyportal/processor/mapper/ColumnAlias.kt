package cz.machovec.lekovyportal.processor.mapper

/**
 * Every CSV-column enum implements this interface so that the generic
 * extension [toSpec] can build a [ColumnSpec] without boilerplate.
 */
interface ColumnAlias {
    val aliases: List<String>
    val required: Boolean
}

/**
 * Converts an enum constant that implements [ColumnAlias]
 * into a [ColumnSpec] used by CsvImporter.
 */
inline fun <reified E> E.toSpec(): ColumnSpec<E>
        where E : Enum<E>, E : ColumnAlias =
    ColumnSpec(this, aliases, required)
