package cz.machovec.lekovyportal.importer.mapper

fun interface RowMapper<E, T> where E : Enum<E> {
    fun map(row: CsvRow<E>): T?
}
