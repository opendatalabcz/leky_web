package cz.machovec.lekovyportal.processor.mapper

/**
 * One logical column in CSV.
 *
 * @property aliases  list of names that can column have in the csv file
 * @property required if required is true and column is not found in the CSV file, exception is thrown
 */
data class ColumnSpec<E : Enum<E>>(
    val key: E,
    val aliases: List<String>,
    val required: Boolean = true
)
